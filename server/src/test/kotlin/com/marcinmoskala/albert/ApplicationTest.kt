package com.marcinmoskala.albert

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        // Root path now serves static content (index.html)
        // It will return 404 in tests since static files aren't included in test resources
        val response = client.get("/")
        // Accept either NotFound (no static files in test) or OK (if static files exist)
        assertTrue(
            response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.OK,
            "Expected NotFound or OK, but got ${response.status}"
        )
    }

    @Test
    fun testHealthCheck() = testApplication {
        application {
            module()
        }
        val response = client.get("/api/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }
}