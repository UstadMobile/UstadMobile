package com.ustadmobile.libuicompose.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
actual fun UstadAsyncImage(
    uri: String,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
) {
    AsyncImage(
        model = remember { Uri.parse(uri) },
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
    )
}

