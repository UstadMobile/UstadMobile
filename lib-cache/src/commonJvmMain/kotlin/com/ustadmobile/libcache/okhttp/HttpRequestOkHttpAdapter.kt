package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.request.HttpRequest
import okhttp3.Request

fun HttpRequest.asOkHttpRequest(): Request {
    return Request.Builder()
        .url(url)
        .headers(headers.asOkHttpHeaders())
        .method(method.name, null)
        .build()
}
