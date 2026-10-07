package com.hussain.learningdashboard.feature.course.domain

import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.feature.course.domain.model.Course
import com.hussain.learningdashboard.feature.course.domain.model.CourseDetail
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first: `observe*` always reads from the local database, `refresh*` pulls from
 * the network into the database. The UI never renders network responses directly.
 */
interface CourseRepository {
    fun observeCourses(): Flow<List<Course>>
    suspend fun refreshCourses(): AppResult<Unit>

    fun observeCourseDetail(courseId: Int): Flow<CourseDetail?>
    suspend fun refreshLessons(courseId: Int): AppResult<Unit>

    suspend fun markLessonCompleted(courseId: Int, lessonId: Int): AppResult<Unit>

    suspend fun clearCache()
}
