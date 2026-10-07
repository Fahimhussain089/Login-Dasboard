package com.hussain.learningdashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.hussain.learningdashboard.core.ui.components.LoadingView
import com.hussain.learningdashboard.core.ui.theme.AppTheme
import com.hussain.learningdashboard.feature.auth.domain.AuthRepository
import com.hussain.learningdashboard.navigation.AppNavHost
import com.hussain.learningdashboard.navigation.AppRoute
import com.hussain.learningdashboard.navigation.DashboardRoute
import com.hussain.learningdashboard.navigation.LoginRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel

/** Decides the first screen from the persisted session, so a logged-in user lands on the dashboard. */
class AppViewModel(authRepository: AuthRepository) : ViewModel() {
    val startDestination: StateFlow<AppRoute?> =
        flow { emit(if (authRepository.isLoggedIn()) DashboardRoute else LoginRoute) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@Composable
fun App() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val appViewModel = koinViewModel<AppViewModel>()
            val startDestination by appViewModel.startDestination.collectAsStateWithLifecycle()

            startDestination?.let { AppNavHost(startDestination = it) } ?: LoadingView()
        }
    }
}
