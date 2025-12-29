package com.marcinmoskala.albert

actual val platform: Platform = Platform.JS
actual val environmentServerUrl: String? =
    (js("typeof process !== 'undefined' && process.env && process.env.SERVER_URL") as? String)
        ?: (js("typeof globalThis !== 'undefined' ? globalThis.SERVER_URL : undefined") as? String)
