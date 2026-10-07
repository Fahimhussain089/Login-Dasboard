package com.hussain.learningdashboard.core.ui

import com.hussain.learningdashboard.core.common.AppError

fun AppError.toMessage(): String = when (this) {
    AppError.NoInternet -> "No internet connection. Check your network and try again."
    AppError.Unauthorized -> "Your session has expired. Please log in again."
    AppError.NotFound -> "We couldn't find what you were looking for."
    is AppError.Server -> "Something went wrong on our side (error $code). Please try again."
    AppError.Unknown -> "Something went wrong. Please try again."
}
