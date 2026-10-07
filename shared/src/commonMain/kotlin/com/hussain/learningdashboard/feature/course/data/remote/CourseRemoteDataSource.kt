package com.hussain.learningdashboard.feature.course.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    @SerialName("lessons") val lessonCount: Int,
)

@Serializable
data class LessonDto(
    val id: Int,
    val title: String,
    val completed: Boolean,
)

interface CourseRemoteDataSource {
    suspend fun fetchCourses(): List<CourseDto>
    suspend fun fetchLessons(courseId: Int): List<LessonDto>
}

class KtorCourseRemoteDataSource(private val client: HttpClient) : CourseRemoteDataSource {
    override suspend fun fetchCourses(): List<CourseDto> = client.get("courses").body()

    override suspend fun fetchLessons(courseId: Int): List<LessonDto> =
        client.get("courses/$courseId/lessons").body()
}
