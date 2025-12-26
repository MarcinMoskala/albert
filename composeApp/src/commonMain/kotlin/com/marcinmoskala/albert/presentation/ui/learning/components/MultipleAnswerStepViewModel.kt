package com.marcinmoskala.albert.presentation.ui.learning.components

import com.marcinmoskala.albert.domain.model.MultipleAnswerStep
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MultipleAnswerStepViewModel(
    private val step: MultipleAnswerStep,
    private val onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow(MultipleAnswerStepUiState())
    val uiState: StateFlow<MultipleAnswerStepUiState> = _uiState.asStateFlow()

    fun toggleAnswer(answer: String) {
        _uiState.update { state ->
            val newAnswers = if (answer in state.selectedAnswers) {
                state.selectedAnswers - answer
            } else {
                state.selectedAnswers + answer
            }
            state.copy(selectedAnswers = newAnswers)
        }
    }

    fun submit() {
        val selectedAnswers = _uiState.value.selectedAnswers
        if (selectedAnswers.isEmpty()) return

        val isCorrect = selectedAnswers.toSet() == step.correct.toSet()

        _uiState.update {
            it.copy(
                isSubmitted = true,
                isCorrect = isCorrect
            )
        }
    }

    fun continueToNext() {
        onAnswerSubmitted(_uiState.value.isCorrect)
    }
}

data class MultipleAnswerStepUiState(
    val selectedAnswers: Set<String> = emptySet(),
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false
)
