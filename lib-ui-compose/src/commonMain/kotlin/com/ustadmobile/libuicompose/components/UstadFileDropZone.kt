package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * File drop zone. Only runs on Desktop (has no effect on Android).
 */
@Composable
expect fun UstadFileDropZone(
    onFileDropped: (UstadFilePickResult) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () ->  Unit,
)
