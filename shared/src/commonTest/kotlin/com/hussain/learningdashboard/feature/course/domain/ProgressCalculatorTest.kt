package com.hussain.learningdashboard.feature.course.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressCalculatorTest {

    @Test
    fun `rounds to the nearest whole percent and guards edge cases`() {
        assertEquals(65, ProgressCalculator.percentage(completed = 13, total = 20))
        assertEquals(33, ProgressCalculator.percentage(completed = 1, total = 3))
        assertEquals(67, ProgressCalculator.percentage(completed = 2, total = 3))
        assertEquals(0, ProgressCalculator.percentage(completed = 0, total = 0))
        assertEquals(100, ProgressCalculator.percentage(completed = 12, total = 10))
    }
}
