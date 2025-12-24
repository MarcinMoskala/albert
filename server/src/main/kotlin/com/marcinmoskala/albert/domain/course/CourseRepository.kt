package com.marcinmoskala.albert.domain.course

import com.marcinmoskala.model.course.CourseApi

interface CourseRepository {
    suspend fun getCourses(): CourseApi
}