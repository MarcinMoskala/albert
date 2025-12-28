package com.marcinmoskala.albert

enum class Platform {
    Android,
    iOS,
    JVM,
    JS
}

expect val platform: Platform

const val SERVER_PORT = 8080

val SERVER_HOST: String
    get() = when (platform) {
        Platform.Android -> "10.0.2.2"
        else -> "localhost"
    }

val SERVER_URL: String
    get() = "http://$SERVER_HOST:$SERVER_PORT"