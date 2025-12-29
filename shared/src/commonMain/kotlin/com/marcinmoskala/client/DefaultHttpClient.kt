package com.marcinmoskala.client

import com.marcinmoskala.albert.SERVER_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun buildDefaultHttpClient(): HttpClient = HttpClient {
    expectSuccess = false

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val errorBody: String = runCatching { response.bodyAsText() }.getOrDefault("")
                throw IllegalStateException("HTTP ${response.status} for ${response.request.url}. Body: $errorBody")
            }
        }
    }

    defaultRequest {
        url(SERVER_URL)
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
}
