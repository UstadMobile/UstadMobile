package com.ustadmobile.libcache

import com.ustadmobile.libcache.headers.HttpHeaders

/**
 * Tells the cache to mark an entry as newly validated and accessed (e.g. after an http not modified
 * response was received from the origin server).
 */
data class ValidatedEntry(
    val url: String,
    val headers: HttpHeaders,
) {
}