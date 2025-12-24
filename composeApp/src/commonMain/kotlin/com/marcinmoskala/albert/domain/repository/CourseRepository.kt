package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.model.course.CourseDefinitionApi
import kotlinx.coroutines.flow.StateFlow

interface CourseRepository {
    val courses: StateFlow<List<CourseDefinitionApi>>
    suspend fun refresh()
}