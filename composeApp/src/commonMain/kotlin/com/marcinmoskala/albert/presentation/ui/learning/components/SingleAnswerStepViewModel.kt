package com.marcinmoskala.albert.presentation.ui.learning.components

import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.database.UserProgressRecord
import com.marcinmoskala.database.UserProgressStatus
import com.marcinmoskala.model.course.SingleAnswerStepApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SingleAnswerStepViewModel(
    private val step: SingleAnswerStep,
    private val courseId: String,
    private val lessonId: String,
    private val onStepCompleted: () -> Unit,
    private val userProgressRepository: UserProgressRepository
) : BaseViewModel() {

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

        if (isCorrect) {
            saveProgress()
            onStepCompleted()
        }
    }

    private fun saveProgress() {
        viewModelScope.launch {
            val now = Clock.System.now()
            val userId = "guest" // TODO: Get actual user ID from auth system

            val record = UserProgressRecord(
                userId = userId,
                courseId = courseId,
                lessonId = lessonId,
                stepId = step.stepId,
                status = UserProgressStatus.COMPLETED,
                createdAt = now,
                updatedAt = now,
                reviewAt = if (step.repeatable) {
                    // Schedule first review in 1 day
                    now.plus(1.days)
                } else null,
                lastIntervalDays = if (step.repeatable) 1 else null
            )

            userProgressRepository.upsert(record)
        }
    }
}

data class SingleAnswerStepUiState(
    val selectedAnswer: String? = null,
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean = false
)
