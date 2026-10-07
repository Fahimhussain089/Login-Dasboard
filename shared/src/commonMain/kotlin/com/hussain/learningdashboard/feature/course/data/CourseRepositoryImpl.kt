package com.hussain.learningdashboard.feature.course.data

import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.core.network.safeCall
import com.hussain.learningdashboard.feature.course.data.local.CourseLocalDataSource
import com.hussain.learningdashboard.feature.course.data.remote.CourseDto
import com.hussain.learningdashboard.feature.course.data.remote.CourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.data.remote.LessonDto
import com.hussain.learningdashboard.feature.course.domain.CourseRepository
import com.hussain.learningdashboard.feature.course.domain.ProgressCalculator
import com.hussain.learningdashboard.feature.course.domain.model.Course
import com.hussain.learningdashboard.feature.course.domain.model.CourseDetail
import com.hussain.learningdashboard.feature.course.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Lesson completion is recorded locally first and is monotonic (a lesson never becomes
 * un-completed), so merging server data is a conflict-free union: completed locally OR remotely.
 */
class CourseRepositoryImpl(
    private val remote: CourseRemoteDataSource,
    private val local: CourseLocalDataSource,
) : CourseRepository {

    override fun observeCourses(): Flow<List<Course>> = local.observeCourses()

    override suspend fun refreshCourses(): AppResult<Unit> = safeCall {
        val remoteCourses = remote.fetchCourses()
        local.withTransaction {
            val merged = remoteCourses.map { dto ->
                val cachedLessons = local.getLessons(dto.id)
                val course = dto.toDomain()
                if (cachedLessons.isEmpty()) {
                    course
                } else {
                    // Local lessons may contain completions the server hasn't seen yet.
                    course.copy(
                        progress = ProgressCalculator.percentage(cachedLessons),
                        totalLessons = cachedLessons.size,
                    )
                }
            }
            local.replaceCourses(merged)
        }
    }

    override fun observeCourseDetail(courseId: Int): Flow<CourseDetail?> =
        combine(local.observeCourse(courseId), local.observeLessons(courseId)) { course, lessons ->
            course?.let { CourseDetail(it, lessons) }
        }

    override suspend fun refreshLessons(courseId: Int): AppResult<Unit> = safeCall {
        val remoteLessons = remote.fetchLessons(courseId)
        local.withTransaction {
            val completedLocally = local.getLessons(courseId)
                .filter { it.isCompleted }
                .mapTo(mutableSetOf()) { it.id }
            val merged = remoteLessons.map { dto ->
                dto.toDomain(courseId).let { it.copy(isCompleted = it.isCompleted || it.id in completedLocally) }
            }
            local.replaceLessons(courseId, merged)
            local.updateCourseProgress(courseId, ProgressCalculator.percentage(merged), merged.size)
        }
    }

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int): AppResult<Unit> = safeCall {
        local.withTransaction {
            local.markLessonCompleted(courseId, lessonId)
            val lessons = local.getLessons(courseId)
            local.updateCourseProgress(courseId, ProgressCalculator.percentage(lessons), lessons.size)
        }
    }

    override suspend fun clearCache() = local.clear()
}

private fun CourseDto.toDomain() = Course(
    id = id,
    title = title,
    instructor = instructor,
    progress = progress.coerceIn(0, 100),
    totalLessons = lessonCount,
)

private fun LessonDto.toDomain(courseId: Int) = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = completed,
)
