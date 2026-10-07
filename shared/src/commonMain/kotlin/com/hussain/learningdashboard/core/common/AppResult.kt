package com.hussain.learningdashboard.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

/** Domain-level error. UI maps it to a user-facing message, data layer maps exceptions to it. */
sealed interface AppError {
    data object NoInternet : AppError
    data object Unauthorized : AppError
    data class Server(val code: Int) : AppError
    data object NotFound : AppError
    data object Unknown : AppError
}
