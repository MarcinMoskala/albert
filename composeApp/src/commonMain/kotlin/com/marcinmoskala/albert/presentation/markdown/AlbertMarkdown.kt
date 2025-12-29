package com.marcinmoskala.albert.presentation.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.ImageTransformer

@Composable
fun AlbertMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    imageTransformer: ImageTransformer = AlbertMarkdownImageTransformer,
) {
    Markdown(
        content = content.rewriteMarkdownImageUrls(),
        modifier = modifier,
        imageTransformer = imageTransformer,
    )
}
