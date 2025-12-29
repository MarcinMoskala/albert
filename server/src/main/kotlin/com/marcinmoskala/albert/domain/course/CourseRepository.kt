package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.Courses

interface CourseRepository {
    suspend fun getCourses(): Courses
}