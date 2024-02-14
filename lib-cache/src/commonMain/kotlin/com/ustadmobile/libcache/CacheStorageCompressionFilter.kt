package com.ustadmobile.libcache

import com.ustadmobile.libcache.headers.HttpHeaders

/**
 * Filter that will be used by the cache to determine if a given entry should be stored using
 * compression. This should generally be true for text types e.g. css, javascript, html, json, etc.
 * Should not be used for types that are already compressed e.g. images, audio, video, zips, etc.
 *
 * When it is determined that an entry should be compressed, then it will be stored on disk as a
 * compressed file. When it is served, the content-encoding header will be used (which is stored
 * together with all other headers when added to the cache).
 */
fun interface CacheStorageCompressionFilter {

    operator fun invoke(url: String, headers: HttpHeaders): CompressionType

}