package com.marcinmoskala.albert.data

import com.marcinmoskala.albert.data.mappers.toDomain
import com.marcinmoskala.albert.domain.model.Course
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.client.CourseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CourseRepositoryImpl(
    private val courseClient: CourseClient
) : CourseRepository {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    override val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    override suspend fun refresh() {
        val response = courseClient.fetchCourses()
        _courses.value = response.courses.map { it.toDomain() }
    }
}