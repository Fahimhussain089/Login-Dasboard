package com.hussain.learningdashboard.feature.auth.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class LoginResponseDto(val token: String, val userName: String)

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequestDto): LoginResponseDto
}

class KtorAuthRemoteDataSource(private val client: HttpClient) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequestDto): LoginResponseDto =
        client.post("auth/login") { setBody(request) }.body()
}
