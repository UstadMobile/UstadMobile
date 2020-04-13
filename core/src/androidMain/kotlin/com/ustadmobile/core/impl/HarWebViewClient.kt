package com.ustadmobile.core.impl

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Message
import android.view.KeyEvent
import android.webkit.*
import androidx.annotation.RequiresApi
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.contentformats.har.HarNameValuePair
import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse
import com.ustadmobile.core.util.UMIOUtils
import org.jsoup.Jsoup


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun WebResourceRequest.toHarRequest(payload: String?): HarRequest {
    val request = HarRequest()
    request.url = this.url.toString()
    request.headers = this.requestHeaders.map { HarNameValuePair(it.key, it.value) }
    request.method = this.method
    request.body = payload
    return request
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
fun HarResponse.toWebResourceResponse(): WebResourceResponse {
    return WebResourceResponse(
            this.content?.mimeType?.split(";")?.get(0) ?: "text/html",
            this.content?.encoding ?: "utf-8",
            if (this.status < 100 || (this.status > 299 || this.status < 400)) 200 else this.status,
            this.statusText ?: "OK",
            this.headers.map { it.name to it.value }.toMap(),
            this.content?.data)
}


class HarWebViewClient(private val harContainer: HarContainer) : WebViewClient() {

    var recorder: PayloadRecorder? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val payload = recorder?.getPayload(request.method, request.url.toString())
        harContainer.serve(request.toHarRequest(payload))
        return super.shouldOverrideUrlLoading(view, request)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val payload = recorder?.getPayload(request.method, request.url.toString())
        val response = harContainer.serve(request.toHarRequest(payload))
        val webResponse = response.toWebResourceResponse()
        if(webResponse.mimeType.startsWith("text/html")){
            val data = UMIOUtils.readStreamToString(webResponse.data)
            val document = Jsoup.parse(data)
            document.head().prepend(jsInject)
            webResponse.data = document.toString().byteInputStream()
        }
        return webResponse
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String, isReload: Boolean) {
        harContainer.checkWithPattern(url)
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    fun setRecoder(recorder: PayloadRecorder) {
        this.recorder = recorder
    }

}

val worker = """
    
     const constantMock = window.fetch;
        window.fetch = function() {
            // Get the parameter in arguments
            // Intercept the parameter here
            
             if(arguments.length > 0){
                 var request = arguments[0].clone()
                console.log(request.bodyUsed)
                if (request.method !== 'GET' && request.method !== 'HEAD') { 
                    request.text().then(function(body) {
                            recorder.recordPayload(request.method, request.url, body);
                    });
                }
            }

            return constantMock.apply(this, arguments);
        };
"""

val jsInject = "<script type=\"text/javascript\">$worker</script>"

class PayloadRecorder {

    private val payloadMap: MutableMap<String, String> =
            mutableMapOf()

    @JavascriptInterface
    fun recordPayload(
            method: String,
            url: String,
            payload: String) {
        payloadMap["$method-$url"] = payload
    }

    fun getPayload(
            method: String,
            url: String
    ): String? =
            payloadMap["$method-$url"]
}