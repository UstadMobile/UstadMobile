package com.ustadmobile.core.impl

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.webkit.*
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.contentformats.har.HarNameValuePair
import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceRequest.toHarRequest(): HarRequest {
    val request = HarRequest()
    request.url = this.url.toString()
    request.headers = this.requestHeaders.map { HarNameValuePair(it.key, it.value) }
    request.method = this.method
    return request
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun HarResponse.toWebResourceResponse(): WebResourceResponse {
    return WebResourceResponse(
            this.content?.mimeType?.split(";")?.get(0) ?: "text/html",
            this.content?.encoding ?: "utf-8",
            if(this.status < 100 || (this.status > 299 || this.status < 400)) 200 else this.status,
            this.statusText ?: "OK",
            this.headers.map { it.name to it.value }.toMap(),
            this.content?.data)
}


class HarWebViewClient(private val harContainer: HarContainer) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        harContainer.serve(request.toHarRequest())
        return super.shouldOverrideUrlLoading(view, request)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val response = harContainer.serve(request.toHarRequest())
        val webResponse = response.toWebResourceResponse()
        return webResponse
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String, isReload: Boolean) {
        harContainer.checkWithPattern(url)
        super.doUpdateVisitedHistory(view, url, isReload)
    }


}