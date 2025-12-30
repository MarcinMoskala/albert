package com.marcinmoskala.albert.endpoints

import com.marcinmoskala.albert.domain.course.CourseService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureCourseRouting() {
    val courseService: CourseService by inject()
    get("/course") {
        call.respond(courseService.getCourses())
    }
}