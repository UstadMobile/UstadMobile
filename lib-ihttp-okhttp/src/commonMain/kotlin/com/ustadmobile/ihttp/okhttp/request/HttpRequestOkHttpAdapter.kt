package com.ustadmobile.ihttp.okhttp.request

import com.ustadmobile.ihttp.okhttp.headers.asOkHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import okhttp3.Request

fun IHttpRequest.asOkHttpRequest(): Request {
    return Request.Builder()
        .url(url)
        .headers(headers.asOkHttpHeaders())
        .method(method.name, null)
        .build()
}
