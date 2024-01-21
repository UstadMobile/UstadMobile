package com.ustadmobile.libuicompose.view.epubcontent

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient

class EpubWebViewClient: WebViewClient() {

    var loaded: Boolean = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        loaded = false
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        loaded = true
        view?.adjustHeightToWrapContent()
    }
}