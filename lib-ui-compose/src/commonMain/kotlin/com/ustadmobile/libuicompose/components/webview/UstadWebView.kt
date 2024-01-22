package com.ustadmobile.libuicompose.components.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun UstadWebView(
    navigator: UstadWebViewNavigator,
    modifier: Modifier = Modifier
)
