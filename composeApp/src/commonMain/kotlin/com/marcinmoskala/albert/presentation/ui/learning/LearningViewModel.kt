package com.marcinmoskala.albert.presentation.ui.learning

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.usecase.SubmitStepAnswerUseCase
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
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

    private val _uiState = MutableStateFlow(LearningUiState(loading = true))
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    init {
        loadLearningContent()
    }

    private fun loadLearningContent() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loading = true, error = null) }

                val courses = courseRepository.courses.value
                val userId = "guest1" // TODO: Get actual user ID from auth system

                val allSteps = when {
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

                // Filter out already completed steps
                val stepsToShow = allSteps.filter { step ->
                    val progress = userProgressRepository.get(
                        userId,
                        courseId ?: "",
                        lessonId ?: "",
                        step.stepId
                    )
                    // Show step if it's not completed or if it's due for review
                    progress == null || progress.reviewAt?.let { it <= kotlin.time.Clock.System.now() } == true
                }

                _uiState.update {
                    it.copy(
                        loading = false,
                        remainingSteps = stepsToShow.toList(),
                        courseId = courseId ?: "",
                        lessonId = lessonId ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        error = e
                    )
                }
            }
        }
    }

    fun onStepAnswered(isCorrect: Boolean) {
        viewModelScope.launch {
            val currentStep = _uiState.value.currentStep ?: return@launch

            // Save progress to database
            submitStepAnswerUseCase.execute(
                step = currentStep,
                courseId = _uiState.value.courseId,
                lessonId = _uiState.value.lessonId,
                isCorrect = isCorrect
            )

            _uiState.update { state ->
                val newRemainingSteps = state.remainingSteps.toMutableList()

                if (isCorrect) {
                    // Remove correctly answered step
                    newRemainingSteps.removeAt(0)
                } else {
                    // Reposition this step to be retried after 5 other steps (or at end if fewer)
                    val incorrectStep = newRemainingSteps.removeAt(0)
                    // Insert at position 5 (which means after 5 other steps), or at the end if list is shorter
                    val insertPosition = minOf(5, newRemainingSteps.size)
                    newRemainingSteps.add(insertPosition, incorrectStep)
                }

                state.copy(
                    remainingSteps = newRemainingSteps,
                    // Increment counter to force UI refresh with new key
                    stepPresentationCounter = state.stepPresentationCounter + 1
                )
            }
        }
    }

    fun retry() {
        loadLearningContent()
    }

    fun onBack() {
        navigator.navigateBack()
    }
}

data class LearningUiState(
    val loading: Boolean = false,
    val remainingSteps: List<LessonStep> = emptyList(),
    val courseId: String = "",
    val lessonId: String = "",
    val error: Throwable? = null,
    val stepPresentationCounter: Int = 0
) {
    val currentStep: LessonStep?
        get() = remainingSteps.firstOrNull()

    val hasSteps: Boolean
        get() = remainingSteps.isNotEmpty()

    val totalSteps: Int
        get() = remainingSteps.size
}
