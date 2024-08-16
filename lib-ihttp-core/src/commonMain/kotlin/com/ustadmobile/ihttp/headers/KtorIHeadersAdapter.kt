package com.ustadmobile.ihttp.headers

import io.ktor.http.Headers

class KtorHeadersAdapter(private val headers: Headers) :IHttpHeaders {

    override fun get(name: String): String? {
        return headers[name]
    }

    override fun getAllByName(name: String): List<String> {
        return headers.getAll(name) ?: emptyList()
    }

    override fun names(): Set<String> {
        return headers.names()
    }

}

fun Headers.asIHttpHeaders(): IHttpHeaders = KtorHeadersAdapter(this)

