package com.marcinmoskala.albert.presentation.ui.learning.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.presentation.markdown.AlbertMarkdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SingleAnswerStepView(
    step: SingleAnswerStep,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    stepKey: Int,
    modifier: Modifier = Modifier,
    viewModel: SingleAnswerStepViewModel = koinViewModel(key = stepKey.toString()) {
        parametersOf(step, onAnswerSubmitted)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AlbertMarkdown(
                content = step.question,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                step.answers.forEach { answer ->
                    AnswerOption(
                        answer = answer,
                        selected = uiState.selectedAnswer == answer,
                        correctAnswer = if (uiState.isSubmitted) step.correct else null,
                        onSelect = { viewModel.selectAnswer(answer) },
                        enabled = !uiState.isSubmitted
                    )
                }
            }

            if (uiState.isSubmitted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isCorrect)
                            CorrectHighlightGreen
                        else
                            MaterialTheme.colorScheme.errorContainer,
                        contentColor = if (uiState.isCorrect)
                            OnCorrectContainerGreen
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.isCorrect) "Correct!" else "Incorrect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        AlbertMarkdown(
                            content = step.explanation,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (uiState.isSubmitted) {
                    viewModel.continueToNext()
                } else {
                    viewModel.submit()
                }
            },
            enabled = uiState.selectedAnswer != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    !uiState.isSubmitted -> "Submit"
                    else -> "Continue"
                }
            )
        }
    }
}

@Composable
private fun AnswerOption(
    answer: String,
    selected: Boolean,
    correctAnswer: String?,
    onSelect: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isCorrect = correctAnswer != null && answer == correctAnswer
    val isWrong = correctAnswer != null && selected && answer != correctAnswer

    val (backgroundColor, contentColor) = when {
        isCorrect -> CorrectHighlightGreen to OnCorrectContainerGreen
        isWrong -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        selected -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                enabled = enabled,
                role = Role.RadioButton
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        contentColor = contentColor,
        border = if (selected && correctAnswer == null) {
            ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled
            )
            AlbertMarkdown(
                content = answer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
