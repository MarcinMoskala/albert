package com.marcinmoskala.albert.presentation.markdown

import com.marcinmoskala.albert.SERVER_URL

/**
 * Rewrites Markdown image destinations so relative URLs like `elvis.png` become
 * `${SERVER_URL}/static/elvis.png`.
 *
 * Rules:
 * - Absolute URLs (e.g. https://..., data:...) are left unchanged
 * - Paths starting with `/` are interpreted as absolute to the server host (prefixed with SERVER_URL)
 * - Other paths are interpreted as relative to `/static/` on the server
 */
fun String.rewriteMarkdownImageUrls(serverUrl: String = SERVER_URL): String {
    val base = serverUrl.trimEnd('/')

    // Avoid Regex here: Kotlin/JS compiles it to a JS RegExp, and some patterns can crash
    // on certain JS engines (Safari) at module init time, blanking the whole screen.
    val out = StringBuilder(length)
    var i = 0
    while (i < length) {
        val start = indexOf("![", startIndex = i)
        if (start == -1) {
            out.append(substring(i))
            break
        }

        out.append(substring(i, start))

        val mid = indexOf("](", startIndex = start + 2)
        if (mid == -1) {
            out.append(substring(start))
            break
        }

        val end = indexOf(")", startIndex = mid + 2)
        if (end == -1) {
            out.append(substring(start))
            break
        }

        val alt = substring(start + 2, mid)
        val inside = substring(mid + 2, end) // destination + optional title

        val firstNonWs = inside.indexOfFirstNonWhitespace()
        if (firstNonWs == -1) {
            out.append(substring(start, end + 1))
            i = end + 1
            continue
        }

        val prefix = inside.substring(0, firstNonWs)
        val trimmed = inside.substring(firstNonWs)

        val (rawDest, rest) = splitDestinationAndRest(trimmed)
        val dest = rawDest.removeSurrounding("<", ">")
        val rewritten = rewriteDestination(base, dest)
        val wrapped = if (rawDest.startsWith("<") && rawDest.endsWith(">")) "<$rewritten>" else rewritten

        out.append("![").append(alt).append("](").append(prefix).append(wrapped).append(rest).append(")")
        i = end + 1
    }

    return out.toString()
}

private fun String.indexOfFirstNonWhitespace(): Int {
    for (idx in indices) {
        if (!this[idx].isWhitespace()) return idx
    }
    return -1
}

private fun splitDestinationAndRest(s: String): Pair<String, String> {
    if (s.isEmpty()) return "" to ""
    return if (s[0] == '<') {
        val close = s.indexOf('>')
        if (close == -1) s to "" else s.substring(0, close + 1) to s.substring(close + 1)
    } else {
        val ws = s.indexOfFirst { it.isWhitespace() }
        if (ws == -1) s to "" else s.substring(0, ws) to s.substring(ws)
    }
}

private fun rewriteDestination(base: String, destination: String): String {
    val trimmed = destination.trim()
    if (trimmed.isBlank()) return destination

    val lower = trimmed.lowercase()
    val isAbsoluteUrl =
        lower.startsWith("http://") ||
            lower.startsWith("https://") ||
            lower.startsWith("data:") ||
            lower.startsWith("file:") ||
            lower.startsWith("content:") ||
            lower.startsWith("android.resource:") ||
            lower.startsWith("res:")

    if (isAbsoluteUrl) return trimmed

    val normalized = trimmed.removePrefix("./").trimStart('/')

    return if (trimmed.startsWith("/")) {
        "$base/${trimmed.trimStart('/')}"
    } else {
        "$base/static/$normalized"
    }
}
