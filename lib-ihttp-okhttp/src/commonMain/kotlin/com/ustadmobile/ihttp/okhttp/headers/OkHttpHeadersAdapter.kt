package com.ustadmobile.ihttp.okhttp.headers

import com.ustadmobile.ihttp.headers.IHttpHeaders
import okhttp3.Headers

class OkHttpHeadersAdapter(
    private val okHttpHeaders: Headers
): IHttpHeaders {
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

fun Headers.asIHttpHeaders() = OkHttpHeadersAdapter(this)
