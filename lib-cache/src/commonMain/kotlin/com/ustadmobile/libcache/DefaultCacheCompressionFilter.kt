package com.ustadmobile.libcache

import com.ustadmobile.libcache.cachecontrol.RequestCacheControlHeader
import com.ustadmobile.libcache.headers.HttpHeaders

class DefaultCacheCompressionFilter: CacheStorageCompressionFilter {

    override fun invoke(
        url: String,
        requestHeaders: HttpHeaders,
        responseHeaders: HttpHeaders,
    ): CompressionType {
        val requestCacheControl = requestHeaders["cache-control"]?.let {
            RequestCacheControlHeader.parse(it)
        }
        val responseCacheControl = responseHeaders["cache-control"]?.let {
            RequestCacheControlHeader.parse(it)
        }

        val noTransform = (requestCacheControl?.noTransform == true) ||
                (responseCacheControl?.noTransform == true)

        /*
         * If the request or response is marked as no-transform, then obey accordingly.
         */
        if(noTransform){
            return CompressionType.byHeaderVal(responseHeaders["content-encoding"])
        }

        val mimeTypeOnly = responseHeaders["content-type"]?.substringBefore(";")
            ?.lowercase()
        return mimeTypeOnly?.let {
            if(it.startsWith("text/") || it in GZIP_APPLICATION_TYPES)
                CompressionType.GZIP
            else
                CompressionType.NONE
        } ?: CompressionType.NONE
    }

    companion object {

        val GZIP_APPLICATION_TYPES = listOf(
            "application/xhtml+xml", "application/json", "application/javascript",
            "application/oebps-package+xml",//OPF package
            "image/svg+xml",//SVG - XML
        )
    }
}