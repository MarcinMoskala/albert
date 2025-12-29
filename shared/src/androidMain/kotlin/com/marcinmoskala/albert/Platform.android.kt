package com.marcinmoskala.albert

import com.marcinmoskala.albert.BuildConfig

actual val platform: Platform = Platform.Android
actual val environmentServerUrl: String? = BuildConfig.SERVER_URL.takeIf { it.isNotBlank() }
