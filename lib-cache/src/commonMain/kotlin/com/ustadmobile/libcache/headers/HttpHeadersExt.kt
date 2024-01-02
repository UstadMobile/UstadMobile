package com.ustadmobile.libcache.headers

/**
 * Turn the headers into a string that can be used for CacheEntry
 */
internal fun HttpHeaders.asString() : String{
    return names().flatMap {name ->
        getAllByName(name).map { HttpHeader(name, it) }
    }.joinToString(separator = "\r\n") { it.asString() }
}

fun HttpHeaders.contentLength(): Long? {
    return this["content-length"]?.toLong()
}

