package com.ustadmobile.libuicompose.components.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import io.github.aakira.napier.Napier
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream

/**
 * Android WebViewClient to push all requests through OKHttp (and therefor through the cache, so
 * any offline content will load as expected).
 */
class OkHttpWebViewClient(
    private val okHttpClient: OkHttpClient
): WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest
    ): WebResourceResponse {
        try {
            val okHttpRequest = Request.Builder()
                .url(request.url.toString())
                .apply {
                    request.requestHeaders.forEach {
                        header(it.key, it.value)
                    }
                }
                .method(request.method, null)
                .build()
            val okHttpResponse = okHttpClient.newCall(okHttpRequest).execute()

            return WebResourceResponse(
                okHttpResponse.header("content-type") ?: "application/octet-stream",
                "utf-8",
                okHttpResponse.code,
                okHttpResponse.message,
                okHttpResponse.headers.toMap(),
                okHttpResponse.body?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
            )
        }catch(e: Throwable) {
            val inStream = ByteArrayInputStream((e.message ?: "").toByteArray())
            Napier.d("OkHttpWebViewClient: could not load: ${request.url}",e)
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                504,
                "Unavailable",
                emptyMap(),
                inStream,
            )
        }

    }
}