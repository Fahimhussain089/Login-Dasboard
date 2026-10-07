package com.hussain.learningdashboard.feature.auth.domain

import com.hussain.learningdashboard.feature.course.domain.CourseRepository

/** Logging out also wipes cached learner data so the next user on this device starts clean. */
class LogoutUseCase(
    private val authRepository: AuthRepository,
    private val courseRepository: CourseRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
        courseRepository.clearCache()
    }
}
