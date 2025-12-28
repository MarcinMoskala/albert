package com.marcinmoskala.client

import com.marcinmoskala.model.course.CoursesApi
import com.marcinmoskala.model.course.CourseApi
import com.marcinmoskala.model.course.LessonApi
import com.marcinmoskala.model.course.SingleAnswerStepApi
import com.marcinmoskala.model.course.MultipleAnswerStepApi
import com.marcinmoskala.model.course.ExactTextStepApi
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class CourseClient(
    private val httpClient: HttpClient
) {
    suspend fun fetchCourses(): CoursesApi = httpClient.get("/course").body()
}