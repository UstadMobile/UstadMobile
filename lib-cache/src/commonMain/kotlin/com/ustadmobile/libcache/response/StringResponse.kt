package com.ustadmobile.libcache.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.libcache.headers.addIntegrity
import com.ustadmobile.libcache.headers.containsHeader
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.asKotlinxIoSource
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.ByteArrayInputStream

class StringResponse(
    override val request: IHttpRequest,
    private val mimeType: String,
    private val extraHeaders: IHttpHeaders = IHttpHeaders.emptyHeaders(),
    override val responseCode: Int = 200,
    age: Int = 0,
    body: String,
): IHttpResponse {

    private val bodyBytes: ByteArray = body.toByteArray()

    override val headers: IHttpHeaders=  iHeadersBuilder {
        takeFrom(extraHeaders)
        val integrity = sha256Integrity(
            bodyBytes.asKotlinxIoSource().buffered().useAndReadSha256())
        header("content-length", bodyBytes.size.toString())
        header("content-type", mimeType)
        addIntegrity(extraHeaders, integrity)
        if(!extraHeaders.containsHeader("age"))
            header("age", age.toString())
    }

    override fun bodyAsSource(): Source {
        return ByteArrayInputStream(bodyBytes).asSource().buffered()
    }
}