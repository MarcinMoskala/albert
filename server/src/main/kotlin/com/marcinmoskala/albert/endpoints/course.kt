package com.marcinmoskala.albert.endpoints

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import com.marcinmoskala.albert.domain.course.CourseService

fun Application.configureCourseRouting() {
    val courseService: CourseService by inject()
    routing {
        get("/course") {
            call.respond(courseService.getCourses())
        }
    }
}