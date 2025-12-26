package com.marcinmoskala.albert.presentation.ui.app

import com.marcinmoskala.albert.domain.model.Course
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.navigation.Navigator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val courseRepository: CourseRepository,
    private val navigator: Navigator,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow(MainUiState(loading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        courseRepository.courses
            .onEach { courses ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        courses = createCoursesUi(courses)
                    )
                }
            }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            courseRepository.refresh()
        }
    }

    fun onCourseClick(courseId: String) {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = courseId,
                lessonId = null
            )
        )
    }

    fun onLessonClick(courseId: String, lessonId: String) {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = courseId,
                lessonId = lessonId
            )
        )
    }

    fun onReviewAllClick() {
        navigator.navigateTo(
            AppDestination.Learning(
                courseId = null,
                lessonId = null
            )
        )
    }

    private fun createCoursesUi(courses: List<Course>): List<CourseMainUi> =
        courses.map { course ->
            CourseMainUi(
                courseId = course.courseId,
                title = course.title,
                lessons = course.lessons.map { lesson ->
                    LessonMainUi(
                        lessonId = lesson.lessonId,
                        name = lesson.name,
                        steps = lesson.steps.size
                    )
                }
            )
    }
}