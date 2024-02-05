package com.ustadmobile.libuicompose.view.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.ustadmobile.core.viewmodel.about.OpenLicensesViewModel
import com.ustadmobile.libuicompose.components.webview.AssetWebViewClient
import com.ustadmobile.libuicompose.components.webview.UstadWebView
import com.ustadmobile.libuicompose.components.webview.UstadWebViewNavigatorAndroid

@Composable
actual fun OpenLicensesScreen(
    viewModel: OpenLicensesViewModel,
) {
    val context = LocalContext.current

    val webViewNavigator = remember {
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(context.applicationContext))
            .build()

        UstadWebViewNavigatorAndroid(AssetWebViewClient(assetLoader, onClickLink = viewModel::onClickLink))
    }

    UstadWebView(
        navigator = webViewNavigator,
        modifier = Modifier.fillMaxSize()
            .testTag("open_licenses")
    )

    LaunchedEffect(Unit){
        webViewNavigator.loadUrl("https://appassets.androidplatform.net/assets/open_source_licenses.html")
    }

}