package com.marcinmoskala.client

import com.marcinmoskala.albert.SERVER_URL
import com.marcinmoskala.model.LoginRequest
import com.marcinmoskala.model.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthClient(private val httpClient: HttpClient) {
    suspend fun login(request: LoginRequest): LoginResponse {
        return httpClient.post("$SERVER_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}
