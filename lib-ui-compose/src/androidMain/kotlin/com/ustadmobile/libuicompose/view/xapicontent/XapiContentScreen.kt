package com.ustadmobile.libuicompose.view.xapicontent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.ustadmobile.core.domain.contententry.server.ContentEntryVersionServerWebClient
import com.ustadmobile.core.util.ext.onActiveEndpoint
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState
import com.ustadmobile.libuicompose.components.UstadWebView
import com.ustadmobile.libuicompose.components.UstadWebViewNavigator
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance

@Composable
actual fun XapiContentScreen(
    uiState: XapiContentUiState
) {
    val di = localDI()
    val webViewNavigator = remember {
        UstadWebViewNavigator(webViewClient = di.onActiveEndpoint().direct.instance<ContentEntryVersionServerWebClient>())
    }

    UstadWebView(webViewNavigator)

    LaunchedEffect(uiState.url) {
        uiState.url?.also {
            webViewNavigator.loadUrl(it)
        }
    }

}