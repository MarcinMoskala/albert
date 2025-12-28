package com.marcinmoskala.albert.endpoints

import com.marcinmoskala.albert.domain.auth.AuthService
import com.marcinmoskala.model.LoginRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureAuthRouting() {
    val authService by inject<AuthService>()

    routing {
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val response = authService.login(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to (e.message ?: "Authentication failed"))
                )
            }
        }
    }
}
