package com.marcinmoskala.albert.presentation.markdown

import androidx.compose.runtime.*
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.Uri
import coil3.request.Options
import coil3.request.ImageRequest
import coil3.size.Size as CoilSize
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.contentType
import okio.Buffer
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter

/**
 * Shared ImageLoader used by Markdown.
 *
 * Builds a platform HttpClient (Ktor engines per target), closes it with the composition,
 * and installs a simple HTTP fetcher so Markdown image URLs (`http/https`) load on every platform.
 *
 * Without it, images won't load in Markdown.
 */
@Composable
internal fun rememberMarkdownImageLoader(): ImageLoader {
    val context = LocalPlatformContext.current
    val httpClient = remember { createMarkdownHttpClient() }

    DisposableEffect(httpClient) {
        onDispose { httpClient.close() }
    }

    return remember(context, httpClient) {
        ImageLoader.Builder(context)
            .components { add(KtorHttpFetcher.Factory(httpClient)) }
            .build()
    }
}

internal object AlbertMarkdownImageTransformer : com.mikepenz.markdown.model.ImageTransformer {

    @Composable
    override fun transform(link: String): com.mikepenz.markdown.model.ImageData {
        val imageLoader = rememberMarkdownImageLoader()
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(link)
                .size(CoilSize.ORIGINAL)
                .build(),
            imageLoader = imageLoader
        )
        LaunchedEffect(painter, link) {
            snapshotFlow { painter.state.value }
                .collectLatest { state ->
                    val label = state::class.simpleName ?: state.toString()
                    val error = (state as? AsyncImagePainter.State.Error)?.result?.throwable?.message
                    val errorLabel = error?.let { ": $it" } ?: ""
                }
        }
        return com.mikepenz.markdown.model.ImageData(painter)
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size {
        var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
        if (painter is AsyncImagePainter) {
            val painterState = painter.state.collectAsState()
            val intrinsicSize = painterState.value.painter?.intrinsicSize
            intrinsicSize?.also { size = it }
        }
        return size
    }
}

internal class KtorHttpFetcher(
    private val url: String,
    private val client: HttpClient,
    private val fileSystem: okio.FileSystem,
) : Fetcher {

    override suspend fun fetch(): FetchResult? =
        runCatching {
            val response = client.get(url)
            val bytes = response.body<ByteArray>()
            val mimeType = response.contentType()?.let { "${it.contentType}/${it.contentSubtype}" }
            val source = ImageSource(
                source = Buffer().write(bytes),
                fileSystem = fileSystem,
            )
            SourceFetchResult(
                source = source,
                mimeType = mimeType,
                dataSource = DataSource.NETWORK,
            )
        }.getOrNull()

    class Factory(private val client: HttpClient) : Fetcher.Factory<Any> {
        override fun create(data: Any, options: Options, imageLoader: ImageLoader): Fetcher? {
            val url = when (data) {
                is String -> data
                is Uri -> data.toString()
                else -> null
            }?.takeIf { it.startsWith("http://") || it.startsWith("https://") } ?: return null
            return KtorHttpFetcher(url, client, options.fileSystem)
        }
    }
}

// Platform-specific client
expect fun createMarkdownHttpClient(): HttpClient