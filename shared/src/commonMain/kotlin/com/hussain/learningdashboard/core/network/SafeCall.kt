package com.hussain.learningdashboard.core.network

import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

class NoInternetException : IOException("No internet connection")

suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    AppResult.Failure(e.toAppError())
}

fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.NoInternet
    is ClientRequestException -> when (response.status) {
        HttpStatusCode.Unauthorized -> AppError.Unauthorized
        HttpStatusCode.NotFound -> AppError.NotFound
        else -> AppError.Server(response.status.value)
    }
    is ServerResponseException -> AppError.Server(response.status.value)
    else -> AppError.Unknown
}
