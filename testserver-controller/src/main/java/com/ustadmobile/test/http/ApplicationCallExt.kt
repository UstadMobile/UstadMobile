package com.ustadmobile.test.http

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.httpMethod
import io.ktor.server.request.receiveStream
import io.ktor.server.response.header
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.core.use
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.internal.http.HttpMethod
import okio.BufferedSink
import java.io.ByteArrayInputStream


/**
 * Respond as a reverse proxy. Proxy the call to the given base URL.
 */
@Suppress("NewApi") //This is JVM, not Android, the warning is wrong
suspend fun ApplicationCall.respondReverseProxy(
    proxyPassUrl: String,
    httpClient: OkHttpClient,
) {
    val requestBuilder = Request.Builder().url(proxyPassUrl)

    request.headers.forEach { headerName, headerValues ->
        headerValues.forEach { headerValue ->
            requestBuilder.addHeader(headerName, headerValue)
        }
    }

    //ProxyPassPreserveHost
    request.headers["host"]?.also { hostHeader ->
        requestBuilder.header("Host", hostHeader)
    }

    val bodyBytes = if(HttpMethod.requiresRequestBody(request.httpMethod.value)) {
        withContext(Dispatchers.IO) {
            receiveStream().readAllBytes()
        }
    }else {
        null
    }

    requestBuilder.method(
        request.httpMethod.value,
        bodyBytes?.let { bodyByteArr ->
            object: RequestBody() {
                override fun contentType(): MediaType? {
                    return request.headers["content-type"]?.toMediaTypeOrNull()
                }

                override fun writeTo(sink: BufferedSink) {
                    sink.write(bodyByteArr)
                }
            }
        }
    )

    val originResponse = httpClient.newCall(requestBuilder.build()).execute()
    respondOkHttpResponse(originResponse)
}

/**
 * Use a response from OKHttp as the response
 */
suspend fun ApplicationCall.respondOkHttpResponse(
    response: Response
) {
    //content-type is controlled by respondOutputStream, cannot be set as a header.
    val engineHeaders = listOf("content-type", "content-length", "transfer-encoding")
    response.headers.filter { it.first.lowercase() !in engineHeaders }.forEach {
        this.response.header(it.first, it.second)
    }

    val responseInput = response.body?.byteStream() ?: ByteArrayInputStream(byteArrayOf())
    respondOutputStream(
        status = HttpStatusCode.fromValue(response.code),
        contentType = response.header("content-type")?.let { ContentType.parse(it) },
        contentLength = response.headers["content-length"]?.toLong(),
    ) {
        responseInput.use { responseIn ->
            responseIn.copyTo(this)
        }
    }
}
