package com.hussain.learningdashboard.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val BASE_URL = "https://api.learningdashboard.mock/v1/"

val AppJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}

fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    // Non-2xx responses throw, so every failure flows through safeCall/toAppError.
    expectSuccess = true
    install(ContentNegotiation) { json(AppJson) }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }
    defaultRequest {
        url(BASE_URL)
        contentType(ContentType.Application.Json)
    }
}
