package com.ustadmobile.libuicompose.util.ext

import com.multiplatform.webview.web.WebViewNavigator

actual fun WebViewNavigator.loadHtmlWorkaround(html: String) {
    loadHtml(html = "", baseUrl = html)
}
