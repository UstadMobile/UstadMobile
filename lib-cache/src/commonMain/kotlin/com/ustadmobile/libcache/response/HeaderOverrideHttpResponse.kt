package com.ustadmobile.libcache.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.Source

internal class HeaderOverrideHttpResponse(
    private val srcResponse: IHttpResponse,
    private val overrideHeaders: IHttpHeaders,
) : IHttpResponse {

    override val responseCode: Int
        get() = srcResponse.responseCode
    override val request: IHttpRequest
        get() = srcResponse.request
    override val headers: IHttpHeaders
        get() = overrideHeaders

    override fun bodyAsSource(): Source? {
        return srcResponse.bodyAsSource()
    }
}

fun IHttpResponse.withOverridenHeaders(
    newHeaders: IHttpHeaders
): IHttpResponse = HeaderOverrideHttpResponse(this, newHeaders)
