package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source

internal class HeaderOverrideHttpResponse(
    private val srcResponse: HttpResponse,
    private val overrideHeaders: HttpHeaders,
) : HttpResponse {

    override val responseCode: Int
        get() = srcResponse.responseCode
    override val request: HttpRequest
        get() = srcResponse.request
    override val headers: HttpHeaders
        get() = overrideHeaders

    override fun bodyAsSource(): Source? {
        return srcResponse.bodyAsSource()
    }
}

fun HttpResponse.withOverridenHeaders(
    newHeaders: HttpHeaders
): HttpResponse = HeaderOverrideHttpResponse(this, newHeaders)
