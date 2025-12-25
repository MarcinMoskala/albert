package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.model.course.CourseApi
import kotlinx.coroutines.flow.StateFlow

interface CourseRepository {
    val courses: StateFlow<List<CourseApi>>
    suspend fun refresh()
}