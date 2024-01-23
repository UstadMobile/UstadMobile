package com.ustadmobile.libuicompose.components.webview

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.telefonica.nestedscrollwebview.NestedScrollWebView
import com.ustadmobile.core.webview.UstadAbstractWebViewClient
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libuicompose.R
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import moe.tlaster.precompose.navigation.BackHandler

/**
 * The primary purpose of this webview is to push requests through the cache
 */
sealed class WebViewCommand(val time: Long)

class WebViewNavigateCommand(
    val url: String,
    time: Long,
): WebViewCommand(time)

class WebViewGoBackCommand(time: Long): WebViewCommand(time)

class UstadWebViewNavigatorAndroid(
    val webViewClient: UstadAbstractWebViewClient
): UstadWebViewNavigator {

    private val _commandFlow = MutableStateFlow<WebViewCommand>(
        WebViewNavigateCommand("", 0)
    )

    internal val commandFlow: Flow<WebViewCommand> = _commandFlow

    override fun loadUrl(url: String) {
        _commandFlow.value = WebViewNavigateCommand(url, systemTimeInMillis())
    }

    override fun goBack() {
        _commandFlow.value = WebViewGoBackCommand(systemTimeInMillis())
    }
}

/**
 * Android has its own WebViewCompat, but this requires feature support, so its safer just to use
 * the tag.
 */
@SuppressLint("WebViewApiAvailability")
private fun WebView.getWebViewClientCompat() : WebViewClient {
    return if(Build.VERSION.SDK_INT >= 26) {
        webViewClient
    }else {
        getTag(R.id.tag_webview_client) as WebViewClient
    }
}

private fun WebView.setWebViewClientCompat(
    webViewClientValue: WebViewClient
) {
    webViewClient = webViewClientValue
    if(Build.VERSION.SDK_INT < 26) {
        setTag(R.id.tag_webview_client, webViewClientValue)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun UstadWebView(
    navigator: UstadWebViewNavigator,
    modifier: Modifier,
) {
    val navigatorAndroid = (navigator as UstadWebViewNavigatorAndroid)
    val currentCommand by
        navigatorAndroid.commandFlow.collectAsState(WebViewNavigateCommand("", 0))

    val webViewCanGoBack by navigatorAndroid.webViewClient.canGoBack.collectAsState(false)
    Napier.d { "WebViewCanGoBack: $webViewCanGoBack" }

    //Might need: https://engineering.telefonica.com/nested-scrolling-with-android-webviews-54e0d67e1c23
    AndroidView(
        modifier = modifier,
        factory = { context ->
            NestedScrollWebView(context).also {
                /*
                 * Setting layoutParams is REQUIRED to make webview understand viewport height,
                 * without which css vh units won't work
                 */
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.mediaPlaybackRequiresUserGesture = false

                it.setWebViewClientCompat(navigator.webViewClient)
            }
        },
        update = { webView ->
            val lastCommand = webView.getTag(R.id.tag_webview_url) as? WebViewCommand
            val currentCommandVal = currentCommand
            if(lastCommand !== currentCommandVal) {
                webView.setTag(R.id.tag_webview_url, currentCommandVal)
                if(currentCommandVal.time != 0L) {
                    when(currentCommandVal) {
                        is WebViewNavigateCommand -> webView.loadUrl(currentCommandVal.url)
                        is WebViewGoBackCommand -> webView.goBack()
                    }
                }
            }

            if(webView.getWebViewClientCompat() !== navigator.webViewClient) {
                webView.setWebViewClientCompat(navigator.webViewClient)
            }
        }
    )

    BackHandler(
        enabled = webViewCanGoBack
    ) {
        navigator.goBack()
    }

}