package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable


@Composable
expect fun rememberUstadFilePickLauncher(
    fileExtensions: List<String> = emptyList(),
    mimeTypes: List<String> = emptyList(),
    onFileSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn
