package com.marcinmoskala.albert.presentation.ui.learning.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcinmoskala.albert.domain.model.ExactTextStep
import com.marcinmoskala.albert.presentation.markdown.AlbertMarkdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ExactTextStepView(
    step: ExactTextStep,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    stepKey: Int,
    modifier: Modifier = Modifier,
    viewModel: ExactTextStepViewModel = koinViewModel(key = stepKey.toString()) {
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
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = uiState.userAnswer,
            onValueChange = { viewModel.updateAnswer(it) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitted,
            label = { Text("Your answer") },
            placeholder = { Text("Type your answer here...") },
            isError = uiState.isSubmitted && !uiState.isCorrect,
            singleLine = true
        )

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
                    Text(
                        text = step.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.isCorrect)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (!uiState.isCorrect) {
                        Text(
                            text = "Expected: ${step.correct.joinToString(" or ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (uiState.isSubmitted) {
                    viewModel.continueToNext()
                } else {
                    viewModel.submit()
                }
            },
            enabled = uiState.userAnswer.isNotBlank(),
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
