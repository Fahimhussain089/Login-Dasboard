package com.hussain.learningdashboard.feature.course.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY sortOrder")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourse(courseId: Int): Flow<CourseEntity?>

    @Upsert
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:ids)")
    suspend fun deleteCoursesNotIn(ids: List<Int>)

    @Query("UPDATE courses SET progress = :progress, totalLessons = :totalLessons WHERE id = :courseId")
    suspend fun updateProgress(courseId: Int, progress: Int, totalLessons: Int)

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    fun observeLessons(courseId: Int): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    suspend fun getLessons(courseId: Int): List<LessonEntity>

    @Upsert
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE courseId = :courseId AND id NOT IN (:ids)")
    suspend fun deleteLessonsNotIn(courseId: Int, ids: List<Int>)

    @Query("DELETE FROM lessons WHERE courseId NOT IN (SELECT id FROM courses)")
    suspend fun deleteOrphanLessons()

    @Query("UPDATE lessons SET isCompleted = 1 WHERE courseId = :courseId AND id = :lessonId")
    suspend fun markLessonCompleted(courseId: Int, lessonId: Int)

    @Query("DELETE FROM lessons")
    suspend fun deleteAllLessons()
}
