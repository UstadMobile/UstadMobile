package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.headers.HttpHeaders
import okhttp3.Headers

/**
 * Convert our HttpHeaders to OkHttpHeaders
 *
 * @param keepHostHeader Normally the host header should be added by OKHttp automatically to match
 *        the actual URL.
 */
fun HttpHeaders.asOkHttpHeaders(
    keepHostHeader: Boolean = false,
): Headers {
    val headerLines = names().flatMap { name ->
        if(keepHostHeader || !name.equals("host", true)) {
            getAllByName(name).flatMap { listOf(name, it) }
        }else {
            emptyList()
        }
    }
    return Headers.headersOf(*headerLines.toTypedArray())
}
