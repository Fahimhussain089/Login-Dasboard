package com.hussain.learningdashboard.feature.auth.data

import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.core.common.map
import com.hussain.learningdashboard.core.network.safeCall
import com.hussain.learningdashboard.feature.auth.data.local.SessionStorage
import com.hussain.learningdashboard.feature.auth.data.remote.AuthRemoteDataSource
import com.hussain.learningdashboard.feature.auth.data.remote.LoginRequestDto
import com.hussain.learningdashboard.feature.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val sessionStorage: SessionStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AppResult<Unit> =
        safeCall { remote.login(LoginRequestDto(email.trim(), password)) }
            .map { response -> sessionStorage.saveToken(response.token) }

    override suspend fun isLoggedIn(): Boolean = sessionStorage.getToken() != null

    override suspend fun logout() = sessionStorage.clear()
}
