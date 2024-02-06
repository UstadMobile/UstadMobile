package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest
import okhttp3.Request

class OkHttpRequestAdapter(
    request: Request
) : HttpRequest {
    override val headers: HttpHeaders = request.headers.asCacheHttpHeaders()

    override val url: String = request.url.toString()

    override val method: HttpRequest.Companion.Method = HttpRequest.Companion.Method.forName(request.method)
}

fun Request.asCacheHttpRequest(): HttpRequest = OkHttpRequestAdapter(this)
