package com.ustadmobile.libuicompose.components.webview

import androidx.compose.runtime.Composable

@Composable
expect fun rememberContentEntryVersionNavigator(
    contentEntryVersionUid: Long
): UstadWebViewNavigator