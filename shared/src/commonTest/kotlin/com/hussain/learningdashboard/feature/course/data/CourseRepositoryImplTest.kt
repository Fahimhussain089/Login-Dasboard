package com.hussain.learningdashboard.feature.course.data

import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.feature.course.fakes.FakeCourseLocalDataSource
import com.hussain.learningdashboard.feature.course.fakes.FakeCourseRemoteDataSource
import com.hussain.learningdashboard.feature.course.fakes.TestData
import com.hussain.learningdashboard.feature.course.fakes.TestData.COURSE_ID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CourseRepositoryImplTest {

    private val remote = FakeCourseRemoteDataSource(
        courses = listOf(TestData.pythonCourse),
        lessons = mapOf(COURSE_ID to TestData.pythonLessons),
    )
    private val local = FakeCourseLocalDataSource()
    private val repository = CourseRepositoryImpl(remote, local)

    @Test
    fun `previously loaded courses stay available when the network is down`() = runTest {
        repository.refreshCourses()

        remote.isOnline = false
        val result = repository.refreshCourses()

        assertEquals(AppResult.Failure(AppError.NoInternet), result)
        val cached = repository.observeCourses().first()
        assertEquals(listOf("Python Programming"), cached.map { it.title })
    }

    @Test
    fun `completing a lesson updates its status and recalculates course progress`() = runTest {
        repository.refreshCourses()
        repository.refreshLessons(COURSE_ID)

        repository.markLessonCompleted(COURSE_ID, lessonId = 102)

        val detail = repository.observeCourseDetail(COURSE_ID).first()!!
        assertTrue(detail.lessons.first { it.id == 102 }.isCompleted)
        assertEquals(2, detail.completedLessons)
        assertEquals(50, detail.course.progress)
        assertEquals(50, repository.observeCourses().first().single().progress)
    }

    @Test
    fun `refreshing from the server keeps lessons the user completed locally`() = runTest {
        repository.refreshCourses()
        repository.refreshLessons(COURSE_ID)
        repository.markLessonCompleted(COURSE_ID, lessonId = 102)

        // The server hasn't seen the completion yet and still reports the old state.
        repository.refreshLessons(COURSE_ID)
        repository.refreshCourses()

        val detail = repository.observeCourseDetail(COURSE_ID).first()!!
        assertTrue(detail.lessons.first { it.id == 102 }.isCompleted)
        assertEquals(50, detail.course.progress)
    }
}
