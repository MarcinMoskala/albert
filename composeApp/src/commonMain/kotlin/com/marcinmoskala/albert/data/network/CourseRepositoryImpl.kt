package com.marcinmoskala.albert.data.network

import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.client.CourseClient
import com.marcinmoskala.model.course.CourseDefinitionApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CourseRepositoryImpl(
    private val courseClient: CourseClient
) : CourseRepository {

    private val _courses = MutableStateFlow<List<CourseDefinitionApi>>(emptyList())
    override val courses: StateFlow<List<CourseDefinitionApi>> = _courses.asStateFlow()

    override suspend fun refresh() {
        val response = courseClient.fetchCourses()
        _courses.value = response.courses
    }
}