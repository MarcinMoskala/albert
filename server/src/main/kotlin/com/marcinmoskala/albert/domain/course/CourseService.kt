package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.CourseApi

class CourseService(
    private val courseRepository: CourseRepository
) {
    suspend fun getCourses(): CourseApi = courseRepository.getCourses()
}