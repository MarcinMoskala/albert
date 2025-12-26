package com.marcinmoskala.albert.presentation.ui.learning.components

import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SingleAnswerStepViewModel(
    private val step: SingleAnswerStep,
    private val onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow(SingleAnswerStepUiState())
    val uiState: StateFlow<SingleAnswerStepUiState> = _uiState.asStateFlow()

    fun selectAnswer(answer: String) {
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    fun submit() {
        val selectedAnswer = _uiState.value.selectedAnswer ?: return
        val isCorrect = selectedAnswer == step.correct

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

data class SingleAnswerStepUiState(
    val selectedAnswer: String? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false
)
