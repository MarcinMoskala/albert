package com.marcinmoskala.albert

import platform.Foundation.NSBundle

actual val platform: Platform = Platform.iOS
actual val environmentServerUrl: String? =
    NSBundle.mainBundle.objectForInfoDictionaryKey("SERVER_URL") as? String
