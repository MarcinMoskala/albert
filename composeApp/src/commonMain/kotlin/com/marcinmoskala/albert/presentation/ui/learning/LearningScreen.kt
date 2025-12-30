package com.marcinmoskala.albert.presentation.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningScreen(
    courseId: String?,
    lessonId: String?,
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
                    IconButton(onClick = viewModel::onBack) {
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
                else -> {
                    LearningContent(
                        uiState = uiState,
                        onAnswerSubmitted = viewModel::onStepAnswered
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningContent(
    uiState: LearningUiState,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
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
                text = "Remaining: ${uiState.remainingSteps}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                // Use key() with a counter to force recomposition when step changes
                // This ensures a fresh ViewModel is created for each step presentation,
                // even if the same step appears multiple times (e.g., after an incorrect answer)
                key(uiState.stepPresentationCounter) {
                    when (step) {
                        is SingleAnswerStep -> SingleAnswerStepView(
                            step = step,
                            onAnswerSubmitted = onAnswerSubmitted,
                            stepKey = uiState.stepPresentationCounter
                        )

                        is MultipleAnswerStep -> MultipleAnswerStepView(
                            step = step,
                            onAnswerSubmitted = onAnswerSubmitted,
                            stepKey = uiState.stepPresentationCounter
                        )

                        is ExactTextStep -> ExactTextStepView(
                            step = step,
                            onAnswerSubmitted = onAnswerSubmitted,
                            stepKey = uiState.stepPresentationCounter
                        )

                        is TextStep -> TextStepView(
                            step = step,
                            onAnswerSubmitted = onAnswerSubmitted,
                            stepKey = uiState.stepPresentationCounter
                        )
                    }
                }
            }
        }
    }
}