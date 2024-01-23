package com.ustadmobile.core.webview

import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WebViewClient that provides observable flows for info we want to show in the UI
 */
abstract class UstadAbstractWebViewClient: WebViewClient() {

    private val _canGoBack = MutableStateFlow(false)

    val canGoBack: Flow<Boolean> = _canGoBack.asStateFlow()

    /**
     * This function will catch updates performed by single page navigation (eg hash changes etc).
     */
    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        _canGoBack.value = view?.canGoBack() ?: false
    }

}