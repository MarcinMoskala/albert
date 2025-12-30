package com.marcinmoskala.albert

import com.marcinmoskala.albert.config.FirebaseConfig
import com.marcinmoskala.albert.di.serverModule
import com.marcinmoskala.albert.endpoints.configureAuthRouting
import com.marcinmoskala.albert.endpoints.configureCourseRouting
import com.marcinmoskala.albert.endpoints.configureProgressRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.io.File

private const val SERVER_PORT_SYSTEM_PROPERTY_NAME = "server.port"
private const val PORT_ENVIRONMENT_VARIABLE_NAME = "PORT"
private const val SERVER_PORT_ENVIRONMENT_VARIABLE_NAME = "SERVER_PORT"
private const val DEFAULT_SERVER_PORT = 8080

private fun resolveServerPort(): Int {
    val fromSystemProperty = System.getProperty(SERVER_PORT_SYSTEM_PROPERTY_NAME)
        ?.trim()
        ?.toIntOrNull()

    if (fromSystemProperty != null) {
        return fromSystemProperty
    }

    val fromPortEnvironmentVariable = System.getenv(PORT_ENVIRONMENT_VARIABLE_NAME)
        ?.trim()
        ?.toIntOrNull()

    if (fromPortEnvironmentVariable != null) {
        return fromPortEnvironmentVariable
    }

    val fromEnvironmentVariable = System.getenv(SERVER_PORT_ENVIRONMENT_VARIABLE_NAME)
        ?.trim()
        ?.toIntOrNull()

    return fromEnvironmentVariable ?: DEFAULT_SERVER_PORT
}

fun main() {
    // Initialize Firebase Admin SDK before starting the server
    FirebaseConfig.initialize()

    val serverPort = resolveServerPort()

    embeddedServer(Netty, port = serverPort, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(extraModules: List<Module> = emptyList()) {
    val log = environment.log

    install(Koin) {
        slf4jLogger()
        allowOverride(true)
        modules(listOf(serverModule) + extraModules)
    }
    install(CORS) {
        anyHost()
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
        // API routes (prefixed with /api)
        route("/api") {
            // Health check endpoint
            get("/health") {
                call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
            }

            configureCourseRouting()
            configureAuthRouting()
            configureProgressRouting()
        }

        // Static content serving
        staticResources("/app/", "static") {
            default("index.html")
        }
        get { call.respondRedirect("/app/") }
        // SPA fallback for deep links under /app/**
        get("/app/{...}") {
            val requestedPath = call.request.path().removePrefix("/app/").trimStart('/')

            // If requesting a static asset (contains an extension), try to serve it directly or 404.
            if (requestedPath.contains('.')) {
                val asset = this::class.java.classLoader.getResource("static/$requestedPath")
                if (asset != null) {
                    val contentType = ContentType.defaultForFilePath(requestedPath)
                    call.respondBytes(asset.readBytes(), contentType)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
                return@get
            }

            // Otherwise serve SPA shell for deep links
            val resource = this::class.java.classLoader.getResource("static/index.html")
            if (resource != null) {
                call.respondBytes(resource.readBytes(), ContentType.Text.Html)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Serve files from the repo's `server/static` directory under `/static/*`
        val staticDir = listOf(
            File("static"),        // when working directory is `server/`
            File("server/static"), // when working directory is repo root
        ).firstOrNull { it.exists() && it.isDirectory }

        staticDir?.let { dir ->
            staticFiles("/static", dir)
        } ?: run {
            // Fallback for packaged deployments (see `server/build.gradle.kts` -> processResources)
            staticResources("/static", "server-static")
            log.warn("Static directory not found; falling back to classpath resources `server-static/`.")
        }
    }
}