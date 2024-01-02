package com.ustadmobile.core.util.stringvalues

import js.iterable.toSet
import web.http.Headers

class JsHeaderStringValuesAdapter(private val headers: Headers): IStringValues {

    override fun get(key: String): String? {
        return headers[key]?.split(",", limit = 2)?.firstOrNull()
    }

    override fun getAll(key: String): List<String> {
        return headers[key]?.split(",") ?: emptyList()
    }

    override fun names(): Set<String> {
        return headers.keys().toSet()
    }
}

fun Headers.asIStringValues() = JsHeaderStringValuesAdapter(this)

