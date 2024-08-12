package com.ustadmobile.ihttp.ktorserver

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.asIHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.IHttpRequestWithByteBody
import com.ustadmobile.ihttp.request.IHttpRequestWithFormUrlEncodedData
import com.ustadmobile.ihttp.request.IHttpRequestWithTextBody
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveStream
import io.ktor.server.request.receiveText
import io.ktor.util.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adapter to convert the Ktor request into a cache request (including headers)
 */
internal class KtorApplicationRequestIHttpRequestAdapter(
    private val applicationRequest: ApplicationRequest
) : IHttpRequestWithTextBody, IHttpRequestWithByteBody, IHttpRequestWithFormUrlEncodedData {
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

    override suspend fun bodyAsBytes(): ByteArray? {
        return try {
            withContext(Dispatchers.IO) {
                applicationRequest.call.receiveStream().readAllBytes()
            }
        }catch(e: Throwable) {
            null
        }
    }

    override suspend fun bodyAsFormUrlEncodedDataMap(): Map<String, List<String>> {
        return applicationRequest.call.receiveParameters().toMap()
    }

    override fun queryParam(name: String): String? {
        return applicationRequest.queryParameters[name]
    }
}

fun ApplicationRequest.toIHttpRequest(): IHttpRequest = KtorApplicationRequestIHttpRequestAdapter(this)
