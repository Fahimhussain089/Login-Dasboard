package com.hussain.learningdashboard.feature.course.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val instructor: String,
    val progress: Int,
    val totalLessons: Int,
    /** Preserves the order the API returned. */
    val sortOrder: Int,
)

@Entity(
    tableName = "lessons",
    primaryKeys = ["courseId", "id"],
    indices = [Index("courseId")],
)
data class LessonEntity(
    val id: Int,
    val courseId: Int,
    val title: String,
    val position: Int,
    val isCompleted: Boolean,
)
