package com.marcinmoskala.albert.presentation.ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Albert") },
                actions = {
                    val tooltipPositionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider()

                    TooltipBox(
                        positionProvider = tooltipPositionProvider,
                        tooltip = { PlainTooltip { Text("Synchronize") } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = viewModel::onSyncClick) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Synchronize")
                        }
                    }

                    TooltipBox(
                        positionProvider = tooltipPositionProvider,
                        tooltip = { PlainTooltip { Text("Reset progress") } },
                        state = rememberTooltipState()
                    ) {
                        IconButton(onClick = viewModel::onResetProgressClick) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset progress")
                        }
                    }
                    if (uiState.isLoggedIn) {
                        TooltipBox(
                            positionProvider = tooltipPositionProvider,
                            tooltip = { PlainTooltip { Text("Logout") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = viewModel::onSignOutClick) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CoursesList(
                courses = uiState.courses,
                onCourseClick = viewModel::onCourseClick,
                onLessonClick = viewModel::onLessonClick,
                onReviewAllClick = viewModel::onReviewAllClick
            )
            if (uiState.loading) CircularProgressIndicator()
            if (uiState.error != null) ErrorView(
                message = uiState.error?.message ?: "Unknown error",
                onRetry = viewModel::refresh
            )
        }
    }
}

@Composable
private fun CoursesList(
    courses: List<CourseMainUi>,
    onCourseClick: (String) -> Unit,
    onLessonClick: (String, String) -> Unit,
    onReviewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Review All button at the top
        item {
            Button(
                onClick = onReviewAllClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Review All")
            }
        }

        items(courses, key = { it.courseId }) { course ->
            CourseCard(
                course = course,
                onCourseClick = onCourseClick,
                onLessonClick = onLessonClick
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: CourseMainUi,
    onCourseClick: (String) -> Unit,
    onLessonClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { onCourseClick(course.courseId) }
                ) {
                    Text("Start Course")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                course.lessons.forEach { lesson ->
                    key(lesson.lessonId) {
                        LessonRow(
                            lesson = lesson,
                            onClick = { onLessonClick(course.courseId, lesson.lessonId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    lesson: LessonMainUi,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Steps: ${lesson.steps}, Remaining: ${lesson.remainingSteps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}