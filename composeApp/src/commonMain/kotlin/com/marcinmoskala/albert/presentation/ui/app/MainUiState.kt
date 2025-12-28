package com.marcinmoskala.albert.presentation.ui.app

data class MainUiState(
    val loading: Boolean = false,
    val courses: List<CourseMainUi> = emptyList(),
    val error: Throwable? = null,
    val isLoggedIn: Boolean = false
)

data class CourseMainUi(
    val courseId: String,
    val title: String,
    val lessons: List<LessonMainUi>
)

data class LessonMainUi(
    val lessonId: String,
    val name: String,
    val steps: Int,
)