package com.hussain.learningdashboard.feature.course.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.core.presentation.RefreshState
import com.hussain.learningdashboard.core.presentation.errorOrNull
import com.hussain.learningdashboard.feature.course.domain.CourseRepository
import com.hussain.learningdashboard.feature.course.domain.model.CourseDetail
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState
    data class Error(val error: AppError) : CourseDetailUiState

    data class Content(
        val detail: CourseDetail,
        val isRefreshing: Boolean,
        /** Latest lesson refresh failed; cached lessons (if any) are still shown. */
        val refreshError: AppError?,
        /** One-off failure of a user action, shown as a snackbar then cleared. */
        val actionError: AppError?,
    ) : CourseDetailUiState
}

class CourseDetailViewModel(
    private val courseId: Int,
    private val courseRepository: CourseRepository,
) : ViewModel() {

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Loading)
    private val actionError = MutableStateFlow<AppError?>(null)
    private var refreshJob: Job? = null

    val uiState: StateFlow<CourseDetailUiState> = combine(
        courseRepository.observeCourseDetail(courseId),
        refreshState,
        actionError,
    ) { detail, refresh, actionError ->
        when {
            detail != null -> CourseDetailUiState.Content(
                detail = detail,
                isRefreshing = refresh is RefreshState.Loading,
                refreshError = refresh.errorOrNull,
                actionError = actionError,
            )
            refresh is RefreshState.Loading -> CourseDetailUiState.Loading
            refresh is RefreshState.Failed -> CourseDetailUiState.Error(refresh.error)
            else -> CourseDetailUiState.Error(AppError.NotFound)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshState.value = RefreshState.Loading
        refreshJob = viewModelScope.launch {
            refreshState.value = when (val result = courseRepository.refreshLessons(courseId)) {
                is AppResult.Success -> RefreshState.Idle
                is AppResult.Failure -> RefreshState.Failed(result.error)
            }
        }
    }

    fun onMarkCompleted(lessonId: Int) {
        viewModelScope.launch {
            val result = courseRepository.markLessonCompleted(courseId, lessonId)
            if (result is AppResult.Failure) actionError.value = result.error
        }
    }

    fun onActionErrorShown() {
        actionError.value = null
    }
}
