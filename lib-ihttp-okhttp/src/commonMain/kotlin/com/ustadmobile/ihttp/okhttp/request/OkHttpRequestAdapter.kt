package com.ustadmobile.ihttp.okhttp.request

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.okhttp.headers.asIHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import okhttp3.Request

class OkHttpRequestAdapter(
    private val request: Request
) : IHttpRequest {
    override val headers: IHttpHeaders = request.headers.asIHttpHeaders()

    override val url: String = request.url.toString()

    override val method: IHttpRequest.Companion.Method = IHttpRequest.Companion.Method.forName(request.method)

    override fun queryParam(name: String): String? {
        return request.url.queryParameterValues(name).firstOrNull()
    }
}

fun Request.asIHttpRequest(): IHttpRequest = OkHttpRequestAdapter(this)
