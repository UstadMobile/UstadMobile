package com.ustadmobile.ihttp.request

import com.ustadmobile.ihttp.headers.IHttpHeaders
import io.ktor.http.Url


open class BaseHttpRequest(
    final override val url: String,
    override val headers: IHttpHeaders,
    override val method: IHttpRequest.Companion.Method,
): IHttpRequest {

    private val urlObj = Url(url)

    override fun queryParam(name: String): String? {
        return urlObj.parameters[name]
    }
}
