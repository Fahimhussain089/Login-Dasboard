package com.hussain.learningdashboard.feature.course.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hussain.learningdashboard.core.common.AppError
import com.hussain.learningdashboard.core.ui.components.LoadingView
import com.hussain.learningdashboard.core.ui.components.MessageView
import com.hussain.learningdashboard.core.ui.components.ProgressSection
import com.hussain.learningdashboard.core.ui.components.StatusBanner
import com.hussain.learningdashboard.core.ui.toMessage
import com.hussain.learningdashboard.feature.course.domain.model.Course
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onCourseClick: (courseId: Int) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLoggedOut()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Courses") },
                actions = { TextButton(onClick = viewModel::logout) { Text("Log out") } },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                DashboardUiState.Loading -> LoadingView()
                DashboardUiState.Empty -> MessageView(
                    title = "No courses yet",
                    message = "Courses you enroll in will show up here.",
                    actionLabel = "Refresh",
                    onAction = viewModel::refresh,
                )
                is DashboardUiState.Error -> MessageView(
                    title = "Couldn't load courses",
                    message = current.error.toMessage(),
                    actionLabel = "Retry",
                    onAction = viewModel::refresh,
                )
                is DashboardUiState.Content -> CourseList(
                    state = current,
                    onCourseClick = onCourseClick,
                    onRefresh = viewModel::refresh,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseList(
    state: DashboardUiState.Content,
    onCourseClick: (Int) -> Unit,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.refreshError?.let { error ->
                item(key = "refresh-error") {
                    StatusBanner(
                        message = error.toStaleDataMessage(),
                        actionLabel = "Retry",
                        onAction = onRefresh,
                    )
                }
            }
            items(state.courses, key = { it.id }) { course ->
                CourseCard(course = course, onContinue = { onCourseClick(course.id) })
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onContinue: () -> Unit) {
    Card(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "by ${course.instructor}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            ProgressSection(progress = course.progress, label = "Progress")
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${course.totalLessons} lessons",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Button(onClick = onContinue) {
                    Text(if (course.progress >= 100) "Review" else "Continue")
                }
            }
        }
    }
}

private fun AppError.toStaleDataMessage(): String =
    if (this == AppError.NoInternet) {
        "You're offline. Showing your saved courses."
    } else {
        "Couldn't refresh. Showing your saved courses."
    }
