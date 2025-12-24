package com.marcinmoskala.client

import com.marcinmoskala.model.course.CourseApi
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class CourseClient(
    private val httpClient: HttpClient
) {
    suspend fun fetchCourses(): CourseApi = httpClient.get("/course").body()
}