package com.ustadmobile.libcache.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream

class ByteArrayResponse(
    override val request: IHttpRequest,
    private val mimeType: String,
    private val extraHeaders: IHttpHeaders = IHttpHeaders.emptyHeaders(),
    override val responseCode: Int = 200,
    private val body: ByteArray,
    private val offset: Int = 0,
    private val length: Int = body.size,
) : IHttpResponse {

    override val headers: IHttpHeaders=  iHeadersBuilder {
        takeFrom(extraHeaders)
        header("content-length", length.toString())
        header("content-type", mimeType)
    }

    override fun bodyAsSource(): Source {
        return ByteArrayInputStream(body, offset, length).asSource().buffered()
    }
}