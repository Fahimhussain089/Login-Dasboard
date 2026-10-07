package com.hussain.learningdashboard.feature.course.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val totalLessons: Int,
)

data class Lesson(
    val id: Int,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
)

data class CourseDetail(
    val course: Course,
    val lessons: List<Lesson>,
) {
    val completedLessons: Int get() = lessons.count { it.isCompleted }
}
