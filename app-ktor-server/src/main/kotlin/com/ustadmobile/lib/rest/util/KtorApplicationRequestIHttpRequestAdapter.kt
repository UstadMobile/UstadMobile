package com.ustadmobile.lib.rest.util

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.ihttp.request.IHttpRequest
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod

/**
 * Adapter to convert the Ktor request into a cache request (including headers)
 */
class KtorApplicationRequestIHttpRequestAdapter(
    private val applicationRequest: ApplicationRequest
) : IHttpRequest {
    override val headers: IHttpHeaders
        get() = applicationRequest.headers.asIHttpHeaders()

    override val url: String
        get() = applicationRequest.clientUrl()

    override val method: IHttpRequest.Companion.Method
        get() = IHttpRequest.Companion.Method.forName(applicationRequest.httpMethod.value)
}

fun ApplicationRequest.toIHttpRequest(): IHttpRequest = KtorApplicationRequestIHttpRequestAdapter(this)
