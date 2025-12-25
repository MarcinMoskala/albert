package com.marcinmoskala.albert

import com.marcinmoskala.albert.di.serverModule
import com.marcinmoskala.albert.endpoints.configureCourseRouting
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(extraModules: List<Module> = emptyList()) {
    install(Koin) {
        slf4jLogger()
        allowOverride(true)
        modules(listOf(serverModule) + extraModules)
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            encodeDefaults = false
            classDiscriminator = "type"
        })
    }
    routing {
        get("/") {
            call.respondText("Works!")
        }
    }
    configureCourseRouting()
}