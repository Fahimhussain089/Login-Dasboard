package com.hussain.learningdashboard.feature.course.domain

import com.hussain.learningdashboard.feature.course.domain.model.Lesson

object ProgressCalculator {

    /** Whole-number percentage, rounded half-up, clamped to 0..100. */
    fun percentage(completed: Int, total: Int): Int {
        if (total <= 0) return 0
        val safeCompleted = completed.coerceIn(0, total)
        return (safeCompleted * 100 + total / 2) / total
    }

    fun percentage(lessons: List<Lesson>): Int =
        percentage(completed = lessons.count { it.isCompleted }, total = lessons.size)
}
