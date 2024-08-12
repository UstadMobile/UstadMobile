package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceRequest
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.iRequestBuilder


fun WebResourceRequest.toCacheRequest(): IHttpRequest {
    val methodName = method
    return iRequestBuilder(url.toString()) {
        method = IHttpRequest.Companion.Method.valueOf(methodName)
        requestHeaders.forEach {
            header(it.key, it.value)
        }
    }
}