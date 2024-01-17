package com.ustadmobile.libuicompose.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
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

/**
 * See https://github.com/UstadMobile/UstadMobile/blob/primary/core/src/androidMain/kotlin/com/ustadmobile/core/impl/HarWebViewClient.kt
 */
class UstadWebViewNavigator(val webViewClient: WebViewClient) {

    private val _commandFlow = MutableStateFlow(WebViewCommand("", 0))

    internal val commandFlow: Flow<WebViewCommand> = _commandFlow

    fun loadUrl(url: String) {
        _commandFlow.value = WebViewCommand(url, systemTimeInMillis())
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun UstadWebView(
    navigator: UstadWebViewNavigator,
) {
    val currentCommand by
        navigator.commandFlow.collectAsState(WebViewCommand("", 0))

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            WebView(context).also {
                it.settings.javaScriptEnabled = true
                it.settings.domStorageEnabled = true
                it.settings.mediaPlaybackRequiresUserGesture = false

                it.webViewClient = navigator.webViewClient
            }
        },
        update = {
            val lastCommand = it.getTag(R.id.tag_webview_url) as? WebViewCommand
            if(lastCommand != currentCommand) {
                it.setTag(R.id.tag_webview_url, currentCommand)
                if(currentCommand.time != 0L)
                    it.loadUrl(currentCommand.url)
            }
        }
    )

}