package com.ustadmobile.libcache

import com.ustadmobile.libcache.headers.HttpHeaders

class DefaultCacheGzipFilter: CacheStorageCompressionFilter {

    override fun invoke(url: String, headers: HttpHeaders): CompressionType {
        val mimeTypeOnly = headers["content-type"]?.substringBefore(";")?.lowercase()
        return mimeTypeOnly?.let {
            if(it.startsWith("text/") || it in GZIP_APPLICATION_TYPES)
                CompressionType.GZIP
            else
                CompressionType.NONE
        } ?: CompressionType.NONE
    }

    companion object {

        val GZIP_APPLICATION_TYPES = listOf(
            "application/xhtml+xml", "application/json", "application/javascript"
        )
    }
}