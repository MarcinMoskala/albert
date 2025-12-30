package com.marcinmoskala.albert.presentation.navigation

private const val APP_BASE_PATH = "/app"

private fun normalizePath(pathWithQuery: String): Pair<String, String?> {
    val (path, query) = pathWithQuery.split("?", limit = 2).let {
        it[0] to it.getOrNull(1)
    }
    val normalizedPath = if (path.startsWith(APP_BASE_PATH)) {
        path.removePrefix(APP_BASE_PATH)
    } else path
    return normalizedPath to query
}

private fun buildQuery(vararg pairs: Pair<String, String?>): String {
    val encoded = pairs.mapNotNull { (k, v) ->
        v?.takeIf { it.isNotEmpty() }?.let { "$k=$it" }
    }
    return if (encoded.isEmpty()) "" else "?${encoded.joinToString("&")}"
}

private fun parseQuery(query: String?): Map<String, String> {
    if (query.isNullOrBlank()) return emptyMap()
    return query.split("&")
        .mapNotNull {
            val (k, v) = it.split("=", limit = 2).let { parts -> parts[0] to parts.getOrNull(1) }
            if (k.isBlank() || v == null) null else k to v
        }
        .toMap()
}

internal fun AppDestination.toBrowserPath(): String = when (this) {
    AppDestination.Main -> APP_BASE_PATH
    AppDestination.Login -> "$APP_BASE_PATH/login"
    is AppDestination.Learning -> buildString {
        append(APP_BASE_PATH)
        append("/learn")
        append(
            buildQuery(
                "courseId" to courseId,
                "lessonId" to lessonId
            )
        )
    }

    AppDestination.ResetProgressDialog -> APP_BASE_PATH
}

internal fun browserPathToDestination(pathWithQuery: String): AppDestination? {
    val (path, query) = normalizePath(pathWithQuery)
    val trimmed = path.trim('/')
    if (trimmed.isEmpty()) return AppDestination.Main
    val segments = trimmed.split("/").filter { it.isNotEmpty() }
    val params = parseQuery(query)
    return when (segments.firstOrNull()) {
        "login" -> AppDestination.Login
        "learn" -> AppDestination.Learning(
            courseId = params["courseId"],
            lessonId = params["lessonId"]
        )

        else -> null
    }
}