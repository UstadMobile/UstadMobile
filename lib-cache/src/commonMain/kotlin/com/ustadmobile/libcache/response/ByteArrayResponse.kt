package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream

class ByteArrayResponse(
    override val request: HttpRequest,
    private val mimeType: String,
    private val extraHeaders: HttpHeaders = HttpHeaders.emptyHeaders(),
    override val responseCode: Int = 200,
    private val body: ByteArray,
    private val offset: Int = 0,
    private val length: Int = body.size,
) : HttpResponse{

    override val headers: HttpHeaders=  headersBuilder {
        takeFrom(extraHeaders)
        header("content-length", length.toString())
        header("content-type", mimeType)
    }

    override fun bodyAsSource(): Source {
        return ByteArrayInputStream(body, offset, length).asSource().buffered()
    }
}