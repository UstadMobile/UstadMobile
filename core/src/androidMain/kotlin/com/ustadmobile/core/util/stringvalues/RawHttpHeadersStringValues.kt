package com.ustadmobile.core.util.stringvalues

import rawhttp.core.RawHttpHeaders

class RawHttpHeadersStringValues(
    private val rawHeaders: RawHttpHeaders
): IStringValues {
    override fun get(key: String): String? = rawHeaders[key]?.firstOrNull()

    override fun getAll(key: String): List<String> {
        return rawHeaders.get(key)
    }

    override fun names(): Set<String> {
        return rawHeaders.uniqueHeaderNames
    }
}

fun RawHttpHeaders.asIStringValues() = RawHttpHeadersStringValues(this)
