package com.ustadmobile.libcache

import com.ustadmobile.ihttp.headers.IHttpHeaders


/**
 * Filter that will be used by the cache to determine if a given entry should be stored using
 * compression. This should generally be true for text types e.g. css, javascript, html, json, etc.
 * Should not be used for types that are already compressed e.g. images, audio, video, zips, etc.
 *
 * When it is determined that an entry should be compressed, then it will be stored on disk as a
 * compressed file. When it is served, if the accept-encoding header indicates that the request will
 * support the content-encoding used, then it is served using the encoding in which it was stored
 * (e.g. unaltered).
 *
 * If the accept-encoding header indicates that the request will not accept the encoding used to
 * store the entry, then the entry will be inflated and it will be served with
 * content-encoding: identity. The content-length header will be included and will be set to the
 * uncompressed length.
 *
 * In almost all cases, the client (e.g. browser/webview) will accept the same encoding that is used
 * to store the entry. In all cases the content-length is provided to ensure that any component
 * that would need to provide progress information can do so.
 */
fun interface CacheStorageCompressionFilter {

    operator fun invoke(
        url: String,
        requestHeaders: IHttpHeaders,
        responseHeaders: IHttpHeaders,
    ): CompressionType

}