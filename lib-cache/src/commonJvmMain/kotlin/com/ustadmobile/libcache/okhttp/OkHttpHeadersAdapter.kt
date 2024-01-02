package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.headers.HttpHeaders
import okhttp3.Headers

class OkHttpHeadersAdapter(
    private val okHttpHeaders: Headers
): HttpHeaders {
    override fun get(name: String): String? {
        return okHttpHeaders[name]
    }

    override fun getAllByName(name: String): List<String> {
        return okHttpHeaders.values(name)
    }

    override fun names(): Set<String> {
        return okHttpHeaders.names()
    }
}

fun Headers.asCacheHttpHeaders() = OkHttpHeadersAdapter(this)
