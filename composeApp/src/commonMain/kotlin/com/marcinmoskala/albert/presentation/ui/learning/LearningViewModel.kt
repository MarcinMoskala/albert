package com.marcinmoskala.albert.presentation.ui.learning

import com.marcinmoskala.albert.domain.model.Course
import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository.Companion.ANONYMOUS_USER_ID
import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.albert.domain.usecase.SubmitStepAnswerUseCase
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.navigation.Navigator
import com.marcinmoskala.database.UserProgressStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

class LearningViewModel(
    private val courseRepository: CourseRepository,
    private val userProgressRepository: UserProgressRepository,
    private val userRepository: UserRepository,
    private val submitStepAnswerUseCase: SubmitStepAnswerUseCase,
    private val navigator: Navigator,
    private val courseId: String?,
    private val lessonId: String?,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {
    private val steps = MutableStateFlow(emptyList<LessonStep>())
    private val activeUserId: String = userRepository.currentUser.value?.userId ?: ANONYMOUS_USER_ID

    private val _uiState = MutableStateFlow(
        LearningUiState(
            currentStep = null,
            remainingSteps = 0
        )
    )
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val courses = ensureCourses()
            val now: Instant = Clock.System.now()
            val lessonSteps = getAllSteps(courses)
                .filter { shouldBeSeen(it, now) }
            steps.value = lessonSteps
            _uiState.update { state ->
                when {
                    lessonSteps.isEmpty() -> state.copy(currentStep = null, remainingSteps = 0)
                    else -> state.copy(currentStep = lessonSteps.first(), remainingSteps = lessonSteps.size)
                }
            }
        }
    }

    private suspend fun shouldBeSeen(step: LessonStep, now: Instant): Boolean {
        val record = userProgressRepository.getProgress(activeUserId, step.stepId)
        return when (record?.status) {
            null, UserProgressStatus.PENDING -> true
            UserProgressStatus.COMPLETED -> false
            UserProgressStatus.REPEATING -> {
                val reviewAt = record.reviewAt ?: return true
                reviewAt < now
            }
        }
    }

    fun onStepAnswered(isCorrect: Boolean) {
        val currentStep = _uiState.value.currentStep ?: return
        val updateRepositoryJob = viewModelScope.launch {
            submitStepAnswerUseCase(
                userId = activeUserId,
                step = currentStep,
                isCorrect = isCorrect,
            )
        }
        steps.update {
            if (isCorrect) {
                it - currentStep
            } else {
                val removed = it - currentStep
                removed.take(5) + currentStep + removed.drop(5)
            }
        }
        val nextStep = steps.value.firstOrNull()
        if (nextStep != null) {
            _uiState.update { state ->
                state.copy(
                    currentStep = nextStep,
                    remainingSteps = steps.value.size,
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

    private suspend fun ensureCourses(): List<Course> {
        val cached = courseRepository.courses.value
        if (cached.isNotEmpty()) return cached
        courseRepository.refresh()
        return courseRepository.courses.filter { it.isNotEmpty() }.first()
    }

    private fun getAllSteps(courses: List<Course>): List<LessonStep> {
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

    private fun List<LessonStep>.reorderForRequestedStep(): List<LessonStep> {
        return this
    }
}

data class LearningUiState(
    val currentStep: LessonStep?,
    val remainingSteps: Int,
    val stepPresentationCounter: Int = 0,
)