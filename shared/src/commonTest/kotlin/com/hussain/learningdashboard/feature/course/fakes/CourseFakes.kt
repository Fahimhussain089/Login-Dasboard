package com.hussain.learningdashboard.feature.course.fakes

import com.hussain.learningdashboard.core.network.NoInternetException
import com.hussain.learningdashboard.feature.course.data.local.CourseLocalDataSource
import com.hussain.learningdashboard.feature.course.data.remote.CourseDto
import com.hussain.learningdashboard.feature.course.data.remote.CourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.data.remote.LessonDto
import com.hussain.learningdashboard.feature.course.domain.model.Course
import com.hussain.learningdashboard.feature.course.domain.model.Lesson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCourseRemoteDataSource(
    var courses: List<CourseDto> = emptyList(),
    var lessons: Map<Int, List<LessonDto>> = emptyMap(),
) : CourseRemoteDataSource {

    var isOnline = true

    override suspend fun fetchCourses(): List<CourseDto> {
        if (!isOnline) throw NoInternetException()
        return courses
    }

    override suspend fun fetchLessons(courseId: Int): List<LessonDto> {
        if (!isOnline) throw NoInternetException()
        return lessons.getValue(courseId)
    }
}

class FakeCourseLocalDataSource : CourseLocalDataSource {

    private val courses = MutableStateFlow<List<Course>>(emptyList())
    private val lessons = MutableStateFlow<Map<Int, List<Lesson>>>(emptyMap())

    override fun observeCourses(): Flow<List<Course>> = courses

    override fun observeCourse(courseId: Int): Flow<Course?> =
        courses.map { list -> list.find { it.id == courseId } }

    override fun observeLessons(courseId: Int): Flow<List<Lesson>> =
        lessons.map { it[courseId].orEmpty() }

    override suspend fun getLessons(courseId: Int): List<Lesson> = lessons.value[courseId].orEmpty()

    override suspend fun replaceCourses(courses: List<Course>) {
        this.courses.value = courses
        val ids = courses.map { it.id }.toSet()
        lessons.update { current -> current.filterKeys { it in ids } }
    }

    override suspend fun replaceLessons(courseId: Int, lessons: List<Lesson>) {
        this.lessons.update { it + (courseId to lessons) }
    }

    override suspend fun updateCourseProgress(courseId: Int, progress: Int, totalLessons: Int) {
        courses.update { list ->
            list.map { if (it.id == courseId) it.copy(progress = progress, totalLessons = totalLessons) else it }
        }
    }

    override suspend fun markLessonCompleted(courseId: Int, lessonId: Int) {
        lessons.update { current ->
            val updated = current[courseId].orEmpty().map { if (it.id == lessonId) it.copy(isCompleted = true) else it }
            current + (courseId to updated)
        }
    }

    override suspend fun clear() {
        courses.value = emptyList()
        lessons.value = emptyMap()
    }

    override suspend fun <R> withTransaction(block: suspend () -> R): R = block()
}

object TestData {
    const val COURSE_ID = 1

    val pythonCourse = CourseDto(
        id = COURSE_ID,
        title = "Python Programming",
        instructor = "John Smith",
        progress = 25,
        lessonCount = 4,
    )

    val pythonLessons = listOf(
        LessonDto(id = 101, title = "Introduction", completed = true),
        LessonDto(id = 102, title = "Variables & Data Types", completed = false),
        LessonDto(id = 103, title = "Functions", completed = false),
        LessonDto(id = 104, title = "OOP", completed = false),
    )
}
