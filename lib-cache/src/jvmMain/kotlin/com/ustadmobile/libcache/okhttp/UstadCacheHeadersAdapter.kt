package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.headers.HttpHeaders
import okhttp3.Headers

/**
 * Convert our HttpHeaders to OKHttpHeaders
 */
fun HttpHeaders.asOkHttpHeaders(): Headers {
    val headerLines = names().flatMap { name ->
        getAllByName(name).flatMap { listOf(name, it) }
    }
    return Headers.headersOf(*headerLines.toTypedArray())
}
