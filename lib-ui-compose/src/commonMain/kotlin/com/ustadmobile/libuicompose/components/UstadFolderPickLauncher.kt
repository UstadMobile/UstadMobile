package com.ustadmobile.libuicompose.components

import androidx.compose.runtime.Composable

@Composable
expect fun rememberUstadFolderPickLauncher(
    onFolderSelected: (UstadFilePickResult) -> Unit,
): LaunchFilePickFn
