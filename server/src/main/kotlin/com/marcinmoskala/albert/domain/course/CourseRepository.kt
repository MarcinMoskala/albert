package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.CoursesApi

interface CourseRepository {
    suspend fun getCourses(): CoursesApi
}