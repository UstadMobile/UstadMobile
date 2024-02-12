package com.ustadmobile.libuicompose.components.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import com.ustadmobile.core.webview.UstadAbstractWebViewClient

class AssetWebViewClient(
    private val assetLoader: WebViewAssetLoader,
    private val onClickLink: (String) -> Unit,
) : UstadAbstractWebViewClient() {
    //As per : https://developer.android.com/develop/ui/views/layout/webapps/load-local-content
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false

        return if(!url.startsWith("https://appassets.androidplatform.net/assets")) {
            onClickLink(url)
            true
        }else {
            false
        }
    }


}