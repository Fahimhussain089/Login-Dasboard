package com.hussain.learningdashboard.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.common.AppResult
import com.hussain.learningdashboard.feature.auth.domain.AuthRepository
import com.hussain.learningdashboard.feature.auth.domain.EmailError
import com.hussain.learningdashboard.feature.auth.domain.LoginValidator
import com.hussain.learningdashboard.feature.auth.domain.PasswordError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: EmailError? = null,
    val passwordError: PasswordError? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val isLoggedIn: Boolean = false,
)

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun onLoginClick() {
        val current = _uiState.value
        if (current.isLoading) return

        val emailError = LoginValidator.validateEmail(current.email)
        val passwordError = LoginValidator.validatePassword(current.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = authRepository.login(current.email, current.password)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                is AppResult.Failure -> _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}
