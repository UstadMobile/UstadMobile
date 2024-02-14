package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.headers.contentLength
import com.ustadmobile.libcache.io.uncompress
import kotlinx.io.Source
import kotlinx.io.buffered

fun HttpResponse.requireHeadersContentLength() : Long{
    return headers.contentLength()
        ?: throw IllegalArgumentException("requireHeadersContentLength: response for ${request.url} " +
                "has no content-length header")
}

fun HttpResponse.bodyAsUncompressedSourceIfContentEncoded(): Source? {
    val compressionType = CompressionType.byHeaderVal(headers["content-encoding"])
    return bodyAsSource()?.buffered()?.uncompress(compressionType)
}
