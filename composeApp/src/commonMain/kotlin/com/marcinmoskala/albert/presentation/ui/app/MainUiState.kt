package com.marcinmoskala.albert.presentation.ui.app

import com.marcinmoskala.model.course.LessonDefinitionApi

data class MainUiState(
    val loading: Boolean = false,
    val courses: List<CourseMainUi> = emptyList(),
    val error: Throwable? = null
)

data class CourseMainUi(
    val courseId: String,
    val title: String,
    val lessons: List<LessonMainUi>
)

data class LessonMainUi(
    val courseId: String,
    val name: String,
    val steps: Int,
)