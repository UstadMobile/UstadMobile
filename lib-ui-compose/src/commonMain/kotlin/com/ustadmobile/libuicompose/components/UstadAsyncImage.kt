package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun UstadAsyncImage(
    uri: String,
    contentDescription: String,
    contentScale: ContentScale,
    modifier: Modifier
)
