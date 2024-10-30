package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable


@Composable
actual fun rememberUstadFolderPickLauncher(
    onFolderSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn {
    return {}
}