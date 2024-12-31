package com.ustadmobile.ihttp.okhttp.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.okhttp.headers.asIHttpHeaders
import com.ustadmobile.ihttp.okhttp.request.asIHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import okhttp3.Response

internal class OkHttpResponseAdapter(
    internal val response: Response,
) : IHttpResponse {

    override val responseCode: Int
        get() = response.code
    override val request: IHttpRequest
        get() = response.request.asIHttpRequest()

    override val headers: IHttpHeaders
        get() = response.headers.asIHttpHeaders()

    override fun bodyAsSource(): Source? {
        return response.body?.byteStream()?.asSource()?.buffered()
    }

}

fun Response.asIHttpResponse() : IHttpResponse = OkHttpResponseAdapter(this)

