package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream

class StringResponse(
    override val request: HttpRequest,
    private val mimeType: String,
    private val extraHeaders: HttpHeaders = HttpHeaders.emptyHeaders(),
    override val responseCode: Int = 200,
    body: String,
): HttpResponse {

    private val bodyBytes: ByteArray = body.toByteArray()

    override val headers: HttpHeaders=  headersBuilder {
        takeFrom(extraHeaders)
        header("content-length", bodyBytes.size.toString())
        header("content-type", mimeType)
    }

    override fun bodyAsSource(): Source {
        return ByteArrayInputStream(bodyBytes).asSource().buffered()
    }
}