package com.marcinmoskala.albert.presentation.ui.learning.components

import com.marcinmoskala.albert.domain.model.TextStep
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TextStepViewModel(
    private val step: TextStep,
    private val onAnswerSubmitted: (isCorrect: Boolean) -> Unit,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow(TextStepUiState())
    val uiState: StateFlow<TextStepUiState> = _uiState.asStateFlow()

    fun markReachedEnd() {
        _uiState.update { it.copy(hasReachedEnd = true) }
    }

    fun complete() {
        if (!_uiState.value.hasReachedEnd) return
        _uiState.update { it.copy(isCompleted = true) }
        onAnswerSubmitted(true)
    }
}

data class TextStepUiState(
    val hasReachedEnd: Boolean = false,
    val isCompleted: Boolean = false
)
