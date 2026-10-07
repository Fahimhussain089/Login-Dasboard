package com.hussain.learningdashboard.feature.course.presentation.detail

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hussain.learningdashboard.core.ui.components.LoadingView
import com.hussain.learningdashboard.core.ui.components.MessageView
import com.hussain.learningdashboard.core.ui.components.ProgressSection
import com.hussain.learningdashboard.core.ui.components.StatusBanner
import com.hussain.learningdashboard.core.ui.toMessage
import com.hussain.learningdashboard.feature.course.domain.model.CourseDetail
import com.hussain.learningdashboard.feature.course.domain.model.Lesson
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    courseId: Int,
    onBack: () -> Unit,
    viewModel: CourseDetailViewModel = koinViewModel(parameters = { parametersOf(courseId) }),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val content = state as? CourseDetailUiState.Content

    LaunchedEffect(content?.actionError) {
        content?.actionError?.let { error ->
            snackbarHostState.showSnackbar(error.toMessage())
            viewModel.onActionErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = content?.detail?.course?.title ?: "Course",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                CourseDetailUiState.Loading -> LoadingView()
                is CourseDetailUiState.Error -> MessageView(
                    title = "Couldn't load course",
                    message = current.error.toMessage(),
                    actionLabel = "Retry",
                    onAction = viewModel::refresh,
                )
                is CourseDetailUiState.Content -> CourseDetailContent(
                    state = current,
                    onRetry = viewModel::refresh,
                    onMarkCompleted = viewModel::onMarkCompleted,
                )
            }
        }
    }
}

@Composable
private fun CourseDetailContent(
    state: CourseDetailUiState.Content,
    onRetry: () -> Unit,
    onMarkCompleted: (lessonId: Int) -> Unit,
) {
    val lessons = state.detail.lessons

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") { CourseHeader(state.detail) }

        if (state.refreshError != null && lessons.isNotEmpty()) {
            item(key = "refresh-error") {
                StatusBanner(
                    message = "Showing saved lessons. ${state.refreshError.toMessage()}",
                    actionLabel = "Retry",
                    onAction = onRetry,
                )
            }
        }

        item(key = "lessons-title") {
            Text(
                text = "Lessons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
        }

        if (lessons.isEmpty()) {
            item(key = "lessons-placeholder") {
                when {
                    state.isRefreshing -> Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                    state.refreshError != null -> StatusBanner(
                        message = "Lessons aren't available offline yet. ${state.refreshError.toMessage()}",
                        actionLabel = "Retry",
                        onAction = onRetry,
                    )
                    else -> Text(
                        text = "No lessons in this course yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        items(lessons, key = { it.id }) { lesson ->
            LessonRow(lesson = lesson, onMarkCompleted = { onMarkCompleted(lesson.id) })
        }
    }
}

@Composable
private fun CourseHeader(detail: CourseDetail) {
    val course = detail.course
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "by ${course.instructor}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            val label = if (detail.lessons.isEmpty()) {
                "${course.totalLessons} lessons"
            } else {
                "${detail.completedLessons} of ${detail.lessons.size} lessons completed"
            }
            ProgressSection(progress = course.progress, label = label)
        }
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onMarkCompleted: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (lesson.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = lesson.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (lesson.isCompleted) "Completed" else "Pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lesson.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            if (!lesson.isCompleted) {
                Spacer(Modifier.width(8.dp))
                FilledTonalButton(onClick = onMarkCompleted) { Text("Mark done") }
            }
        }
    }
}
