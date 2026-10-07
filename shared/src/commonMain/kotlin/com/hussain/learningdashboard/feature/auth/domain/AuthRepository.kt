package com.hussain.learningdashboard.feature.auth.domain

import com.hussain.learningdashboard.core.common.AppResult

interface AuthRepository {
    suspend fun login(email: String, password: String): AppResult<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun logout()
}
