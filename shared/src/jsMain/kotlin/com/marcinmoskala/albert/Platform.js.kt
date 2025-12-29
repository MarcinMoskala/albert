package com.marcinmoskala.albert

actual val platform: Platform = Platform.JS
actual val environmentServerUrl: String? =
    (js("typeof window !== 'undefined' && window.location ? window.location.origin : undefined") as? String)
