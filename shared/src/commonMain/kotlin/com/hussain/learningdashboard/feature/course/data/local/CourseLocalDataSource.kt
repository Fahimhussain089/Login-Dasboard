package com.hussain.learningdashboard.feature.course.data.local

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.hussain.learningdashboard.core.database.AppDatabase
import com.hussain.learningdashboard.feature.course.domain.model.Course
import com.hussain.learningdashboard.feature.course.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Thin persistence boundary so the repository's merge logic can be unit tested with an
 * in-memory fake. Multi-step writes must be wrapped in [withTransaction] by the caller.
 */
interface CourseLocalDataSource {
    fun observeCourses(): Flow<List<Course>>
    fun observeCourse(courseId: Int): Flow<Course?>
    fun observeLessons(courseId: Int): Flow<List<Lesson>>
    suspend fun getLessons(courseId: Int): List<Lesson>

    /** Replaces the course list, preserving the given order and dropping courses no longer returned. */
    suspend fun replaceCourses(courses: List<Course>)
    suspend fun replaceLessons(courseId: Int, lessons: List<Lesson>)
    suspend fun updateCourseProgress(courseId: Int, progress: Int, totalLessons: Int)
    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)
    suspend fun clear()

    suspend fun <R> withTransaction(block: suspend () -> R): R
}

class RoomCourseLocalDataSource(private val database: AppDatabase) : CourseLocalDataSource {

    private val dao = database.courseDao()

    override fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { entities -> entities.map { it.toDomain() } }

    override fun observeCourse(courseId: Int): Flow<Course?> =
        dao.observeCourse(courseId).map { it?.toDomain() }

    override fun observeLessons(courseId: Int): Flow<List<Lesson>> =
        dao.observeLessons(courseId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getLessons(courseId: Int): List<Lesson> =
        dao.getLessons(courseId).map { it.toDomain() }

    override suspend fun replaceCourses(courses: List<Course>) {
        dao.upsertCourses(courses.mapIndexed { index, course -> course.toEntity(sortOrder = index) })
        dao.deleteCoursesNotIn(courses.map { it.id })
        dao.deleteOrphanLessons()
    }

    override suspend fun replaceLessons(courseId: Int, lessons: List<Lesson>) {
        dao.upsertLessons(lessons.mapIndexed { index, lesson -> lesson.toEntity(position = index) })
        dao.deleteLessonsNotIn(courseId, lessons.map { it.id })
    }

    override suspend fun updateCourseProgress(courseId: Int, progress: Int, totalLessons: Int) =
        dao.updateProgress(courseId, progress, totalLessons)

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) =
        dao.markLessonCompleted(courseId, lessonId)

    override suspend fun clear() = withTransaction {
        dao.deleteAllLessons()
        dao.deleteAllCourses()
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R =
        database.useWriterConnection { transactor -> transactor.immediateTransaction { block() } }
}

private fun CourseEntity.toDomain() = Course(
    id = id,
    title = title,
    instructor = instructor,
    progress = progress,
    totalLessons = totalLessons,
)

private fun Course.toEntity(sortOrder: Int) = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
    progress = progress,
    totalLessons = totalLessons,
    sortOrder = sortOrder,
)

private fun LessonEntity.toDomain() = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = isCompleted,
)

private fun Lesson.toEntity(position: Int) = LessonEntity(
    id = id,
    courseId = courseId,
    title = title,
    position = position,
    isCompleted = isCompleted,
)
