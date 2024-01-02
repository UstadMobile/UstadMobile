package com.ustadmobile.core.util.stringvalues

import io.ktor.http.Headers

class KtorHeadersStringValues(private val headers: Headers) : IStringValues{

    override fun get(key: String): String? {
        return headers[key]
    }

    override fun getAll(key: String): List<String> {
        return headers.getAll(key) ?: emptyList()
    }

    override fun names(): Set<String> {
        return headers.names()
    }
}

fun Headers.asIStringValues() = KtorHeadersStringValues(this)

