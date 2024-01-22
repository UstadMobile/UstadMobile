package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable


@Composable
expect fun rememberUstadFilePickLauncher(
    onFileSelected: (UstadFilePickResult) -> Unit
): LaunchFilePickFn
