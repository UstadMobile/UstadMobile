package com.ustadmobile.lib.rest.util

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveText

/**
 * Adapter to convert the Ktor request into a cache request (including headers)
 */
class KtorApplicationRequestIHttpRequestAdapter(
    private val applicationRequest: ApplicationRequest
) : IHttpRequestWithTextBody {
    override val headers: IHttpHeaders
        get() = applicationRequest.headers.asIHttpHeaders()

    override val url: String
        get() = applicationRequest.clientUrl()

    override val method: IHttpRequest.Companion.Method
        get() = IHttpRequest.Companion.Method.forName(applicationRequest.httpMethod.value)

    override suspend fun bodyAsText(): String? {
        return try {
            applicationRequest.call.receiveText()
        }catch(e: Throwable) {
            null
        }
    }

    override fun queryParam(name: String): String? {
        return applicationRequest.queryParameters[name]
    }
}

fun ApplicationRequest.toIHttpRequest(): IHttpRequest = KtorApplicationRequestIHttpRequestAdapter(this)
