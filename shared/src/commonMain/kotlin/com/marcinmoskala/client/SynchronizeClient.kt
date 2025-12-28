package com.marcinmoskala.client

import com.marcinmoskala.model.UserCourseProgressApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SynchronizeClient(
    private val httpClient: HttpClient,
) {
    suspend fun synchronize(payload: UserCourseProgressApi): UserCourseProgressApi =
        httpClient.put("/synchronize") {
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
}
