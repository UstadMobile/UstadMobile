package com.ustadmobile.ihttp.response

import com.ustadmobile.ihttp.ext.asSource
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.request.IHttpRequest
import kotlinx.io.Source
import kotlinx.io.buffered

class ByteArrayResponse(
    override val request: IHttpRequest,
    private val mimeType: String,
    private val extraHeaders: IHttpHeaders = IHttpHeaders.emptyHeaders(),
    private val bodyBytes: ByteArray,
    override val responseCode: Int = 200,
) : IHttpResponse{


    override val headers: IHttpHeaders=  iHeadersBuilder {
        takeFrom(extraHeaders)
        header("content-length", bodyBytes.size.toString())
        header("content-type", mimeType)
    }

    override fun bodyAsSource(): Source {
        return bodyBytes.asSource().buffered()
    }

}