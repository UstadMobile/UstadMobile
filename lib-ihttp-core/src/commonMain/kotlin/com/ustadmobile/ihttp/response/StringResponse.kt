package com.ustadmobile.ihttp.response

import com.ustadmobile.ihttp.ext.asSource
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.ihttp.request.IHttpRequest
import io.ktor.utils.io.core.toByteArray
import kotlinx.io.Source
import kotlinx.io.buffered

class StringResponse(
    override val request: IHttpRequest,
    private val mimeType: String,
    private val extraHeaders: IHttpHeaders = IHttpHeaders.emptyHeaders(),
    override val responseCode: Int = 200,
    body: String,
): IHttpResponse {

    private val bodyBytes: ByteArray = body.toByteArray()

    override val headers: IHttpHeaders=  iHeadersBuilder {
        takeFrom(extraHeaders)
        header("content-length", bodyBytes.size.toString())
        header("content-type", mimeType)
    }

    override fun bodyAsSource(): Source {
        return bodyBytes.asSource().buffered()
    }

}
