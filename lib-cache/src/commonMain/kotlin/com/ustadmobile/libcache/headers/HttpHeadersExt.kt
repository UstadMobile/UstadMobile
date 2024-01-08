package com.ustadmobile.libcache.headers

import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_ETAG_IS_INTEGRITY
import com.ustadmobile.libcache.headers.CouponHeader.Companion.HEADER_X_INTEGRITY

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

/**
 * By default we will use the integrity string as the etag so that validation works as expected
 * in a distributed fashion.
 */
fun HttpHeaders.integrity(): String? {
    return if(get(HEADER_ETAG_IS_INTEGRITY) == "true") {
        get("etag")
    }else {
        get(HEADER_X_INTEGRITY)
    }
}

fun HttpHeaders.requireIntegrity(): String {
    return integrity() ?: throw IllegalStateException("Headers do not include integrity")
}


