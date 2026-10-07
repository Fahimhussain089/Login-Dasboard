package com.hussain.learningdashboard.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.hussain.learningdashboard.feature.auth.presentation.LoginScreen
import com.hussain.learningdashboard.feature.course.presentation.dashboard.DashboardScreen
import com.hussain.learningdashboard.feature.course.presentation.detail.CourseDetailScreen
import kotlinx.serialization.Serializable

sealed interface AppRoute

@Serializable
data object LoginRoute : AppRoute

@Serializable
data object DashboardRoute : AppRoute

@Serializable
data class CourseDetailRoute(val courseId: Int) : AppRoute

@Composable
fun AppNavHost(startDestination: AppRoute) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(DashboardRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<DashboardRoute> { entry ->
            DashboardScreen(
                onCourseClick = { courseId ->
                    if (entry.isResumed()) navController.navigate(CourseDetailRoute(courseId))
                },
                onLoggedOut = {
                    navController.navigate(LoginRoute) {
                        popUpTo<DashboardRoute> { inclusive = true }
                    }
                },
            )
        }
        composable<CourseDetailRoute> { entry ->
            CourseDetailScreen(
                courseId = entry.toRoute<CourseDetailRoute>().courseId,
                onBack = { if (entry.isResumed()) navController.popBackStack() },
            )
        }
    }
}

/** Ignores repeated taps while a navigation transition is already running. */
private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
