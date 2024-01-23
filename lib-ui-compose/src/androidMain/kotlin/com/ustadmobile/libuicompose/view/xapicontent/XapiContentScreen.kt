package com.ustadmobile.libuicompose.view.xapicontent

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState
import com.ustadmobile.libuicompose.components.webview.UstadWebView
import com.ustadmobile.libuicompose.components.webview.rememberContentEntryVersionNavigator

@Composable
actual fun XapiContentScreen(
    uiState: XapiContentUiState
) {
    val webViewNavigator = rememberContentEntryVersionNavigator(
        contentEntryVersionUid = uiState.contentEntryVersionUid
    )

    UstadWebView(
        navigator = webViewNavigator,
        modifier = Modifier.fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .testTag("xapi_webview")
    )

    LaunchedEffect(uiState.url) {
        uiState.url?.also {
            webViewNavigator.loadUrl(it)
        }
    }
}