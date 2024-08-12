package com.ustadmobile.ihttp.headers

/**
 * Convert this list of http headers to a single string (e.g. to store in db)
 */
fun List<IHttpHeader>.toHeaderString(): String {
    return joinToString(separator = "\r\n") { "${it.name}: ${it.value}" }
}

fun List<IHttpHeader>.appendOrReplace(name: String, value: String): List<IHttpHeader> {
    val otherHeaders = filter { it.name.equals(name, ignoreCase = true) }
    return buildList {
        addAll(otherHeaders)
        add(IHttpHeaderImpl(name, value))
    }
}
