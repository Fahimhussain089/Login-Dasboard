package com.hussain.learningdashboard.core.presentation

import com.hussain.learningdashboard.core.common.AppError

/** Status of the latest network refresh, combined with cached data to build screen state. */
sealed interface RefreshState {
    data object Idle : RefreshState
    data object Loading : RefreshState
    data class Failed(val error: AppError) : RefreshState
}

val RefreshState.errorOrNull: AppError? get() = (this as? RefreshState.Failed)?.error
