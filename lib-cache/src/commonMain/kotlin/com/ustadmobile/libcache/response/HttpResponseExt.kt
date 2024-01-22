package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.contentLength

fun HttpResponse.requireHeadersContentLength() : Long{
    return headers.contentLength()
        ?: throw IllegalArgumentException("requireHeadersContentLength: response for ${request.url} " +
                "has no content-length header")
}