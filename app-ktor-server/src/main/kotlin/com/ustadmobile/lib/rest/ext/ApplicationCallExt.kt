package com.ustadmobile.lib.rest.ext

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.internal.http.HttpMethod
import okio.BufferedSink
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.io.ByteArrayInputStream

/**
 * Given a proxy to base url, resolve the url for the current call.
 *
 * e.g.
 * proxyToBaseUrl = http://localhost:8080/
 * call.request.uri = /some/file
 *
 * result = http://localhost:8080/some/file
 */
fun ApplicationCall.resolveProxyToUrl(proxyToBaseUrl: String): String {
    return proxyToBaseUrl.removeSuffix("/") + request.uri +
            request.queryParameters.toQueryParamString()
}


/**
 * Respond as a reverse proxy. Proxy the call to the given base URL.
 */
suspend fun ApplicationCall.respondReverseProxy(proxyToBaseUrl: String) {
    val requestBuilder = Request.Builder().url(resolveProxyToUrl(proxyToBaseUrl))
    val di: DI by closestDI()
    val httpClient: OkHttpClient by di.instance()

    request.headers.forEach { headerName, headerValues ->
        headerValues.forEach { headerValue ->
            requestBuilder.addHeader(headerName, headerValue)
        }
    }

    val bodyBytes = if(HttpMethod.requiresRequestBody(request.httpMethod.value)) {
        withContext(Dispatchers.IO) {
            receiveStream().readAllBytes()
        }
    }else {
        null
    }

    requestBuilder.method(request.httpMethod.value, bodyBytes?.let { bodyByteArr ->
        object: RequestBody() {
            override fun contentType(): MediaType? {
                return request.headers["content-type"]?.toMediaTypeOrNull()
            }

            override fun writeTo(sink: BufferedSink) {
                sink.write(bodyByteArr)
            }
        }
    })

    val originResponse = httpClient.newCall(requestBuilder.build()).execute()

    //content-type is controlled by respondOutputStream, cannot be set as a header.
    val engineHeaders = listOf("content-type", "content-length", "transfer-encoding")
    originResponse.headers.filter { it.first.lowercase() !in engineHeaders }.forEach {
        response.header(it.first, it.second)
    }

    val responseInput = originResponse.body?.byteStream() ?: ByteArrayInputStream(byteArrayOf())
    respondOutputStream(status = HttpStatusCode.fromValue(originResponse.code),
        contentType = originResponse.header("content-type")?.let { ContentType.parse(it) }
    ) {
        responseInput.use { responseIn ->
            responseIn.copyTo(this)
        }
    }
}