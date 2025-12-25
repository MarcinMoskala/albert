package com.marcinmoskala.albert.presentation.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcinmoskala.albert.presentation.ui.learning.components.SingleAnswerStepView
import com.marcinmoskala.albert.presentation.ui.learning.components.MultipleAnswerStepView
import com.marcinmoskala.albert.presentation.ui.learning.components.ExactTextStepView
import com.marcinmoskala.albert.presentation.ui.learning.components.TextStepView
import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.domain.model.MultipleAnswerStep
import com.marcinmoskala.albert.domain.model.ExactTextStep
import com.marcinmoskala.albert.domain.model.TextStep
import com.marcinmoskala.model.course.ExactTextStepApi
import com.marcinmoskala.model.course.MultipleAnswerStepApi
import com.marcinmoskala.model.course.SingleAnswerStepApi
import com.marcinmoskala.model.course.TextStepApi
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    courseId: String?,
    lessonId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LearningViewModel = koinViewModel { parametersOf(courseId, lessonId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            courseId == null && lessonId == null -> "Review All"
                            lessonId != null -> "Lesson"
                            else -> "Course"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
            when {
                uiState.loading -> {
                    CircularProgressIndicator()
                }

                uiState.error != null -> {
                    ErrorView(
                        message = uiState.error?.message ?: "Unknown error",
                        onRetry = viewModel::retry
                    )
                }

                uiState.steps.isEmpty() -> {
                    EmptyView()
                }

                else -> {
                    LearningContent(
                        uiState = uiState,
                        onNext = viewModel::nextStep,
                        onPrevious = viewModel::previousStep
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningContent(
    uiState: LearningUiState,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress indicator
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Step ${uiState.currentStepIndex + 1} of ${uiState.totalSteps}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (uiState.currentStepIndex + 1).toFloat() / uiState.totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }

        // Step content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            uiState.currentStep?.let { step ->
                when (step) {
                    is SingleAnswerStep -> SingleAnswerStepView(
                        step = step,
                        courseId = uiState.courseId,
                        lessonId = uiState.lessonId,
                        onStepCompleted = onNext
                    )

                    is MultipleAnswerStep -> MultipleAnswerStepView(
                        step = step,
                        courseId = uiState.courseId,
                        lessonId = uiState.lessonId,
                        onStepCompleted = onNext
                    )

                    is ExactTextStep -> ExactTextStepView(
                        step = step,
                        courseId = uiState.courseId,
                        lessonId = uiState.lessonId,
                        onStepCompleted = onNext
                    )

                    is TextStep -> TextStepView(
                        step = step,
                        courseId = uiState.courseId,
                        lessonId = uiState.lessonId,
                        onStepCompleted = onNext
                    )
                }
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
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun EmptyView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No learning content available",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
