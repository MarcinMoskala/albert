package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.Courses

class CourseService(
    private val courseRepository: CourseRepository
) {
    suspend fun getCourses(): Courses = courseRepository.getCourses()
}