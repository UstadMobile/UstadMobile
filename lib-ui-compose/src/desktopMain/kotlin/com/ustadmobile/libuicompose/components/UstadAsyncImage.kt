package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import java.net.URI

@Composable
actual fun UstadAsyncImage(
    uri: String,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    val uriObj = remember(uri) { URI(uri) }
    KamelImage(
        resource = asyncPainterForUri(uriObj),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

