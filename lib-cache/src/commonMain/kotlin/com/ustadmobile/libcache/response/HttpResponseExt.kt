package com.ustadmobile.libcache.response

import com.ustadmobile.ihttp.headers.contentLength
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.io.uncompress
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.readString

fun IHttpResponse.requireHeadersContentLength() : Long{
    return headers.contentLength()
        ?: throw IllegalArgumentException("requireHeadersContentLength: response for ${request.url} " +
                "has no content-length header")
}

fun IHttpResponse.bodyAsUncompressedSourceIfContentEncoded(): Source? {
    val compressionType = CompressionType.byHeaderVal(headers["content-encoding"])
    return bodyAsSource()?.buffered()?.uncompress(compressionType)
}

fun IHttpResponse.bodyAsString(): String? {
    return bodyAsUncompressedSourceIfContentEncoded()?.use { it.readString() }
}
