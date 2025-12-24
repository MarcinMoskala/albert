package com.marcinmoskala.client

import com.marcinmoskala.albert.SERVER_URL
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun buildDefaultHttpClient(): HttpClient = HttpClient {
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
