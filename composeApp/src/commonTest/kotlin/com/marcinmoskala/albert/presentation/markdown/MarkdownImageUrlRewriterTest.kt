package com.marcinmoskala.albert.presentation.markdown

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownImageUrlRewriterTest {

    private val base = "https://example.com"

    @Test
    fun rewritesSimpleRelativeImageToStatic() {
        val input = "![](elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![]($base/static/elvis.png)", out)
    }

    @Test
    fun rewritesRelativePathWithSubdirToStatic() {
        val input = "![alt](imgs/elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![alt]($base/static/imgs/elvis.png)", out)
    }

    @Test
    fun rewritesDotSlashRelativeToStatic() {
        val input = "![](./elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![]($base/static/elvis.png)", out)
    }

    @Test
    fun rewritesLeadingSlashAsAbsoluteToServerRoot() {
        val input = "![](/static/elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![]($base/static/elvis.png)", out)
    }

    @Test
    fun keepsAbsoluteHttpUrlUnchanged() {
        val input = "![](https://cdn.example.com/elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals(input, out)
    }

    @Test
    fun keepsDataUrlUnchanged() {
        val input = "![](data:image/png;base64,AAA)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals(input, out)
    }

    @Test
    fun preservesTitleAfterUrl() {
        val input = "![](elvis.png \"Elvis\")"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![]($base/static/elvis.png \"Elvis\")", out)
    }

    @Test
    fun preservesAngleBracketedDestination() {
        val input = "![](<elvis.png>)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals("![](<$base/static/elvis.png>)", out)
    }

    @Test
    fun rewritesMultipleImagesInSingleString() {
        val input = """
            Text
            ![](a.png)
            More
            ![](b.png "B")
        """.trimIndent()

        val out = input.rewriteMarkdownImageUrls(serverUrl = base)
        assertEquals(
            """
            Text
            ![]($base/static/a.png)
            More
            ![]($base/static/b.png "B")
            """.trimIndent(),
            out
        )
    }

    @Test
    fun trimsTrailingSlashFromServerUrl() {
        val input = "![](elvis.png)"
        val out = input.rewriteMarkdownImageUrls(serverUrl = "$base/")
        assertEquals("![]($base/static/elvis.png)", out)
    }
}
