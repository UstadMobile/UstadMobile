package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceRequest
import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.request.requestBuilder


fun WebResourceRequest.toCacheRequest(): HttpRequest {
    val methodName = method
    return requestBuilder(url.toString()) {
        requestHeaders.forEach {
            header(it.key, it.value)
            method = HttpRequest.Companion.Method.valueOf(methodName)
        }
    }
}