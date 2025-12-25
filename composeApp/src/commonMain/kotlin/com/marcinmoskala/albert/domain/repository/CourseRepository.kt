package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.albert.domain.model.Course
import kotlinx.coroutines.flow.StateFlow

interface CourseRepository {
    val courses: StateFlow<List<Course>>
    suspend fun refresh()
}