package com.marcinmoskala.albert

enum class Platform {
    Android,
    iOS,
    JVM,
    JS
}

expect val platform: Platform
expect val environmentServerUrl: String?

const val SERVER_PORT = 8080

private val localServerHost: String
    get() = when (platform) {
        Platform.Android -> "10.0.2.2"
        else -> "localhost"
    }

val SERVER_URL: String
    get() = environmentServerUrl?.takeIf { it.isNotBlank() }
        ?: "http://$localServerHost:$SERVER_PORT"