package com.marcinmoskala.albert.presentation.ui.learning.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcinmoskala.albert.domain.model.MultipleAnswerStep
import com.marcinmoskala.albert.presentation.markdown.AlbertMarkdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MultipleAnswerStepView(
    step: MultipleAnswerStep,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    stepKey: Int,
    modifier: Modifier = Modifier,
    viewModel: MultipleAnswerStepViewModel = koinViewModel(key = stepKey.toString()) {
        parametersOf(step, onAnswerSubmitted)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AlbertMarkdown(
            content = step.question,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            step.answers.forEach { answer ->
                MultipleAnswerOption(
                    answer = answer,
                    selected = answer in uiState.selectedAnswers,
                    correctAnswers = if (uiState.isSubmitted) step.correct else null,
                    onToggle = { viewModel.toggleAnswer(answer) },
                    enabled = !uiState.isSubmitted
                )
            }
        }

        if (uiState.isSubmitted) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isCorrect)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (uiState.isCorrect) "Correct!" else "Incorrect",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isCorrect)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    AlbertMarkdown(
                        content = step.explanation,
                        modifier = Modifier.fillMaxWidth()
                    )
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
            enabled = uiState.selectedAnswers.isNotEmpty(),
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
private fun MultipleAnswerOption(
    answer: String,
    selected: Boolean,
    correctAnswers: List<String>?,
    onToggle: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val isCorrect = correctAnswers != null && answer in correctAnswers
    val isWrong = correctAnswers != null && selected && answer !in correctAnswers
    val isMissed = correctAnswers != null && !selected && answer in correctAnswers

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onToggle,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = when {
            isCorrect && selected -> MaterialTheme.colorScheme.primaryContainer
            isWrong -> MaterialTheme.colorScheme.errorContainer
            isMissed -> MaterialTheme.colorScheme.tertiaryContainer
            selected -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (selected && correctAnswers == null) {
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
            Checkbox(
                checked = selected,
                onCheckedChange = null,
                enabled = enabled
            )
            AlbertMarkdown(
                content = answer,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
