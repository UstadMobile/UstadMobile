package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.addIntegrity
import com.ustadmobile.libcache.headers.containsHeader
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.asKotlinxIoSource
import com.ustadmobile.libcache.io.useAndReadSha256
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
    age: Int = 0,
    body: String,
): HttpResponse {

    private val bodyBytes: ByteArray = body.toByteArray()

    override val headers: HttpHeaders=  headersBuilder {
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