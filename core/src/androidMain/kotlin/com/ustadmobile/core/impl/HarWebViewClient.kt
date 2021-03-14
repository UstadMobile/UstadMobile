package com.ustadmobile.core.impl

import android.annotation.TargetApi
import android.os.Build
import android.webkit.*
import androidx.annotation.RequiresApi
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.contentformats.har.HarNameValuePair
import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse
import com.ustadmobile.core.io.RangeInputStream
import com.ustadmobile.core.io.ext.openInputStream
import com.ustadmobile.lib.util.RANGE_CONTENT_RANGE_HEADER


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
    val headerMap = this.headers.map { it.name to it.value }.toMap()

    val entryFile = content?.entryFile

    // range request
    if(status == 206 && entryFile != null){

        val rangeString = headerMap[RANGE_CONTENT_RANGE_HEADER]?.substringAfter("bytes ")
                ?.substringBefore("/")?.split("-") ?: listOf()
        val fromByte: Long = rangeString[0].toLong()
        val toByte: Long = rangeString[1].toLong()

        return WebResourceResponse(
                this.content?.mimeType?.split(";")?.get(0) ?: "text/html",
                this.content?.encoding ?: "utf-8",
                if (this.status < 100 || (this.status > 299 || this.status < 400)) 200 else this.status,
                this.statusText ?: "OK",
                headerMap,
                RangeInputStream(entryFile.openInputStream(),fromByte, toByte))
    }

    return WebResourceResponse(
            this.content?.mimeType?.split(";")?.get(0) ?: "text/html",
            this.content?.encoding ?: "utf-8",
            if (this.status < 100 || (this.status > 299 || this.status < 400)) 200 else this.status,
            this.statusText ?: "OK",
            headerMap,
            this.content?.entryFile?.openInputStream())
}


@ExperimentalStdlibApi
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
        var payload = recorder?.getPayload(request.method, request.url.toString())
        if(request.requestHeaders.containsKey("content-type")){
            while(payload == null){
                payload = recorder?.getPayload(request.method, request.url.toString())
            }
        }
        val response = harContainer.serve(request.toHarRequest(payload))
        return response.toWebResourceResponse()
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String, isReload: Boolean) {
        harContainer.checkWithPattern(url)
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    fun setRecoder(recorder: PayloadRecorder) {
        this.recorder = recorder
    }

}



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