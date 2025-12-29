package com.marcinmoskala.albert

actual val platform: Platform = Platform.JVM
actual val environmentServerUrl: String? = System.getenv("SERVER_URL")
