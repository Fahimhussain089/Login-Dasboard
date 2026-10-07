package com.hussain.learningdashboard.feature.course.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.core.presentation.RefreshState
import com.hussain.learningdashboard.core.presentation.errorOrNull
import com.hussain.learningdashboard.feature.auth.domain.LogoutUseCase
import com.hussain.learningdashboard.feature.course.domain.CourseRepository
import com.hussain.learningdashboard.feature.course.domain.model.Course
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val error: AppError) : DashboardUiState

    /** [refreshError] is set when cached courses are shown because the latest refresh failed. */
    data class Content(
        val courses: List<Course>,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : DashboardUiState
}

class DashboardViewModel(
    private val courseRepository: CourseRepository,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Loading)
    private var refreshJob: Job? = null

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    val uiState: StateFlow<DashboardUiState> =
        combine(courseRepository.observeCourses(), refreshState, ::toUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshState.value = RefreshState.Loading
        refreshJob = viewModelScope.launch {
            refreshState.value = when (val result = courseRepository.refreshCourses()) {
                is AppResult.Success -> RefreshState.Idle
                is AppResult.Failure -> RefreshState.Failed(result.error)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _isLoggedOut.value = true
        }
    }

    private fun toUiState(courses: List<Course>, refresh: RefreshState): DashboardUiState = when {
        courses.isNotEmpty() -> DashboardUiState.Content(
            courses = courses,
            isRefreshing = refresh is RefreshState.Loading,
            refreshError = refresh.errorOrNull,
        )
        refresh is RefreshState.Loading -> DashboardUiState.Loading
        refresh is RefreshState.Failed -> DashboardUiState.Error(refresh.error)
        else -> DashboardUiState.Empty
    }
}
