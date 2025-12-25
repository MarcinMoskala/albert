package com.marcinmoskala.albert.presentation.ui.learning

import com.marcinmoskala.albert.domain.model.LessonStep
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LearningViewModel(
    private val courseRepository: CourseRepository,
    private val courseId: String?,
    private val lessonId: String?
) : BaseViewModel() {

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

                val steps = when {
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

                _uiState.update {
                    it.copy(
                        loading = false,
                        steps = steps,
                        currentStepIndex = 0,
                        totalSteps = steps.size,
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

    fun nextStep() {
        _uiState.update { state ->
            val nextIndex = (state.currentStepIndex + 1).coerceAtMost(state.totalSteps - 1)
            state.copy(currentStepIndex = nextIndex)
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val prevIndex = (state.currentStepIndex - 1).coerceAtLeast(0)
            state.copy(currentStepIndex = prevIndex)
        }
    }

    fun retry() {
        loadLearningContent()
    }
}

data class LearningUiState(
    val loading: Boolean = false,
    val steps: List<LessonStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val courseId: String = "",
    val lessonId: String = "",
    val error: Throwable? = null
) {
    val currentStep: LessonStep?
        get() = steps.getOrNull(currentStepIndex)

    val hasNext: Boolean
        get() = currentStepIndex < totalSteps - 1

    val hasPrevious: Boolean
        get() = currentStepIndex > 0
}
