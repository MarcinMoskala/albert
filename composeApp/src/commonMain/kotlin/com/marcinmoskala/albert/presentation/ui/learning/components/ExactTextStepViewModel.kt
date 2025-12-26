package com.marcinmoskala.albert.presentation.ui.learning.components

import com.marcinmoskala.albert.domain.model.ExactTextStep
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExactTextStepViewModel(
    private val step: ExactTextStep,
    private val onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow(ExactTextStepUiState())
    val uiState: StateFlow<ExactTextStepUiState> = _uiState.asStateFlow()

    fun updateAnswer(answer: String) {
        _uiState.update { it.copy(userAnswer = answer) }
    }

    fun submit() {
        val userAnswer = _uiState.value.userAnswer.trim()
        if (userAnswer.isBlank()) return

        val isCorrect = step.correct.any { correctAnswer ->
            correctAnswer.trim().equals(userAnswer, ignoreCase = true)
        }

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

data class ExactTextStepUiState(
    val userAnswer: String = "",
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false
)
