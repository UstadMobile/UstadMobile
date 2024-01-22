package com.ustadmobile.lib.rest.util

import com.ustadmobile.lib.rest.ext.clientUrl
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.asCacheHeaders
import com.ustadmobile.libcache.request.HttpRequest
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod

/**
 * Adapter to convert the Ktor request into a cache request (including headers)
 */
class KtorApplicationRequestCacheRequestAdapter(
    private val applicationRequest: ApplicationRequest
) : HttpRequest {
    override val headers: HttpHeaders
        get() = applicationRequest.headers.asCacheHeaders()

    override val url: String
        get() = applicationRequest.clientUrl()
    override val method: HttpRequest.Companion.Method
        get() = HttpRequest.Companion.Method.forName(applicationRequest.httpMethod.value)
}

fun ApplicationRequest.toCacheHttpRequest(): HttpRequest = KtorApplicationRequestCacheRequestAdapter(this)
