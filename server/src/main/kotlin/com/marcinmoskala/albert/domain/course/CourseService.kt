package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.CoursesApi

class CourseService(
    private val courseRepository: CourseRepository
) {
    suspend fun getCourses(): CoursesApi = courseRepository.getCourses()
}