package com.ustadmobile.libcache.headers

import io.ktor.http.Headers

class KtorHeadersAdapter(private val headers: Headers) :HttpHeaders {

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

fun Headers.asCacheHeaders(): HttpHeaders = KtorHeadersAdapter(this)

