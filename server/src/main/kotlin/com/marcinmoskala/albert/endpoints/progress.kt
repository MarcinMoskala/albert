package com.marcinmoskala.albert.endpoints

import com.marcinmoskala.albert.domain.progress.ProgressService
import com.marcinmoskala.model.UserCourseProgressApi
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureProgressRouting() {
    val progressService by inject<ProgressService>()

    put("/synchronize") {
        val progress = call.receive<UserCourseProgressApi>()
        val synchronized = progressService.synchronize(progress)
        call.respond(HttpStatusCode.OK, synchronized)
    }
}
