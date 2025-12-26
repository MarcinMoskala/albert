package com.marcinmoskala.albert.presentation.ui.learning.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcinmoskala.albert.domain.model.TextStep
import com.mikepenz.markdown.m3.Markdown
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TextStepView(
    step: TextStep,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TextStepViewModel = koinViewModel {
        parametersOf(step, onAnswerSubmitted)
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Mark as viewed when we reach the bottom
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.value >= scrollState.maxValue - 100 && !uiState.hasReachedEnd) {
            viewModel.markReachedEnd()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scrollable markdown content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Markdown(
                content = step.text,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }

        // Complete button - only enabled when reached end
        Button(
            onClick = { viewModel.complete() },
            enabled = uiState.hasReachedEnd && !uiState.isCompleted,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isCompleted) "Completed" else "Complete"
            )
        }
    }
}
