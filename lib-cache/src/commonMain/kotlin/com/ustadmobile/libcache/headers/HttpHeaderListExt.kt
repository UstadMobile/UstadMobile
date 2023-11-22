package com.ustadmobile.libcache.headers

/**
 * Convert this list of http headers to a single string (e.g. to store in db)
 */
fun List<HttpHeader>.toHeaderString(): String {
    return joinToString(separator = "\r\n") { "${it.name}: ${it.value}" }
}

fun List<HttpHeader>.appendOrReplace(name: String, value: String): List<HttpHeader> {
    val otherHeaders = filter { it.name.equals(name, ignoreCase = true) }
    return buildList {
        addAll(otherHeaders)
        add(HttpHeader(name, value))
    }
}
