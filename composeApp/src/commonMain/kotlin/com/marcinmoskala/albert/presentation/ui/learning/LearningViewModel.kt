package com.marcinmoskala.albert.presentation.ui.learning

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.usecase.SubmitStepAnswerUseCase
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LearningViewModel(
    private val courseRepository: CourseRepository,
    private val userProgressRepository: UserProgressRepository,
    private val submitStepAnswerUseCase: SubmitStepAnswerUseCase,
    private val navigator: Navigator,
    private val courseId: String?,
    private val lessonId: String?,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {
    private val steps = getSteps().toMutableList()

    private val _uiState = MutableStateFlow(LearningUiState(
        currentStep = steps.firstOrNull(),
        remainingSteps = steps.size
    ))
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        if (steps.isEmpty()) {
            navigator.navigateBack()
        }
    }

    fun onStepAnswered(isCorrect: Boolean) {
        val currentStep = _uiState.value.currentStep ?: return
        val updateRepositoryJob = viewModelScope.launch {
            submitStepAnswerUseCase(
                step = currentStep,
                isCorrect = isCorrect,
            )
        }
        if (isCorrect) {
            steps.removeAt(0)
        } else {
            val incorrectStep = steps.removeAt(0)
            val insertPosition = minOf(5, steps.size)
            steps.add(insertPosition, incorrectStep)
        }
        val nextStep = steps.firstOrNull()
        if (nextStep != null) {
            _uiState.update { state ->
                state.copy(
                    currentStep = nextStep,
                    remainingSteps = steps.size,
                    stepPresentationCounter = state.stepPresentationCounter + 1,
                )
            }
        } else {
            viewModelScope.launch {
                updateRepositoryJob.join()
                navigator.navigateTo(AppDestination.Main)
            }
        }
    }

    fun onBack() {
        navigator.navigateBack()
    }

    private fun getSteps(): List<LessonStep> {
        val courses = courseRepository.courses.value
        return when {
            // Review all mode - collect all steps from all lessons
            courseId == null && lessonId == null -> {
                courses.flatMap { course ->
                    course.lessons.flatMap { lesson ->
                        lesson.steps
                    }
                }
            }
            // Specific lesson mode
            courseId != null && lessonId != null -> {
                val course = courses.find { it.courseId == courseId }
                val lesson = course?.lessons?.find { it.lessonId == lessonId }
                lesson?.steps ?: emptyList()
            }
            // Entire course mode
            courseId != null && lessonId == null -> {
                val course = courses.find { it.courseId == courseId }
                course?.lessons?.flatMap { it.steps } ?: emptyList()
            }

            else -> emptyList()
        }
    }
}

data class LearningUiState(
    val currentStep: LessonStep?,
    val remainingSteps: Int,
    val stepPresentationCounter: Int = 0,
)