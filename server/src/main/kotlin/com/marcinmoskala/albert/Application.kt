package com.marcinmoskala.albert

import com.marcinmoskala.albert.config.FirebaseConfig
import com.marcinmoskala.albert.di.serverModule
import com.marcinmoskala.albert.endpoints.configureAuthRouting
import com.marcinmoskala.albert.endpoints.configureCourseRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

private const val SERVER_PORT_SYSTEM_PROPERTY_NAME = "server.port"
private const val SERVER_PORT_ENVIRONMENT_VARIABLE_NAME = "SERVER_PORT"

private fun resolveServerPort(): Int {
    val fromSystemProperty = System.getProperty(SERVER_PORT_SYSTEM_PROPERTY_NAME)
        ?.trim()
        ?.toIntOrNull()

    if (fromSystemProperty != null) {
        return fromSystemProperty
    }

    val fromEnvironmentVariable = System.getenv(SERVER_PORT_ENVIRONMENT_VARIABLE_NAME)
        ?.trim()
        ?.toIntOrNull()

    return fromEnvironmentVariable ?: SERVER_PORT
}

fun main() {
    // Initialize Firebase Admin SDK before starting the server
    FirebaseConfig.initialize()

    val serverPort = resolveServerPort()

    embeddedServer(Netty, port = serverPort, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(extraModules: List<Module> = emptyList()) {
    install(Koin) {
        slf4jLogger()
        allowOverride(true)
        modules(listOf(serverModule) + extraModules)
    }
    install(CORS) {
        allowHost("localhost:8080")
        allowHost("127.0.0.1:8080")
        allowHost("0.0.0.0:8080")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowCredentials = true
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            encodeDefaults = false
            classDiscriminator = "type"
        })
    }
    routing {
        // Health check endpoint
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
        }

        // API routes
        configureCourseRouting()
        configureAuthRouting()

        // Serve static JS client from resources
        staticResources("/app/", "static") {
            default("index.html")
        }
        get { call.respondRedirect("/app/") }
    }
}