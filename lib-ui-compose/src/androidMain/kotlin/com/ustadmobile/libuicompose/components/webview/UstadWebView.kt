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
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libuicompose.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * The primary purpose of this webview is to push requests through the cache
 */

data class WebViewCommand(
    val url: String,
    val time: Long,
)

class UstadWebViewNavigatorAndroid(
    val webViewClient: WebViewClient
): UstadWebViewNavigator {

    private val _commandFlow = MutableStateFlow(WebViewCommand("", 0))

    internal val commandFlow: Flow<WebViewCommand> = _commandFlow

    override fun loadUrl(url: String) {
        _commandFlow.value = WebViewCommand(url, systemTimeInMillis())
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
        navigatorAndroid.commandFlow.collectAsState(WebViewCommand("", 0))

    //Might need: https://engineering.telefonica.com/nested-scrolling-with-android-webviews-54e0d67e1c23
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).also {
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
                it.isVerticalScrollBarEnabled = true

                it.setWebViewClientCompat(navigator.webViewClient)
            }
        },
        update = {
            val lastCommand = it.getTag(R.id.tag_webview_url) as? WebViewCommand
            if(lastCommand != currentCommand) {
                it.setTag(R.id.tag_webview_url, currentCommand)
                if(currentCommand.time != 0L)
                    it.loadUrl(currentCommand.url)
            }

            if(it.getWebViewClientCompat() !== navigator.webViewClient) {
                it.setWebViewClientCompat(navigator.webViewClient)
            }
        }
    )

}