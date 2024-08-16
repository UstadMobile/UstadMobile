package com.ustadmobile.ihttp.request

import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url


class BaseHttpRequest(
    override val url: String,
    override val headers: IHttpHeaders,
    override val method: IHttpRequest.Companion.Method,
    private val body: ByteArray? = null,
): IHttpRequest, IHttpRequestWithByteBody, IHttpRequestWithTextBody {

    private val urlObj = Url(url)

    override fun queryParam(name: String): String? {
        return urlObj.parameters[name]
    }

    override suspend fun bodyAsBytes(): ByteArray? {
        return body
    }

    override suspend fun bodyAsText(): String? {
        return body?.decodeToString()
    }
}
