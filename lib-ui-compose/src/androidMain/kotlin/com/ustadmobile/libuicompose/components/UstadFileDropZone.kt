package com.ustadmobile.libuicompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun UstadFileDropZone(
    onFileDropped: (UstadFilePickResult) -> Unit,
    modifier: Modifier,
    content: @Composable () ->  Unit,
){
    Box(modifier = modifier) {
        content()
    }
}
