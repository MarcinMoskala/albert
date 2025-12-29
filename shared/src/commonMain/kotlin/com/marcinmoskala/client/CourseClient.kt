package com.marcinmoskala.client

import com.marcinmoskala.model.course.CoursesApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class CourseClient(
    private val httpClient: HttpClient
) {
    suspend fun fetchCourses(): CoursesApi = httpClient.get("/course").body()
}