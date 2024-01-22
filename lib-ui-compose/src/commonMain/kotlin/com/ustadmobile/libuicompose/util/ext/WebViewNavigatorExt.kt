package com.ustadmobile.libuicompose.util.ext

import com.multiplatform.webview.web.WebViewNavigator

/**
 * Temporary workaround until https://github.com/KevinnZou/compose-webview-multiplatform/issues/62
 * is merged
 */
expect fun WebViewNavigator.loadHtmlWorkaround(html: String)
