package com.ustadmobile.core.contentformats

import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.StringResponse

/**
 * Simple shortcut extension function that is commonly used by importers to store the ContentManifest
 */
fun UstadCache.storeText(
    url: String,
    text: String,
    mimeType: String,
    cacheControl: String = "immutable"
) {
    val request = requestBuilder(url)
    store(
        storeRequest = listOf(
            CacheEntryToStore(
                request = request,
                response = StringResponse(
                    request = request,
                    mimeType = mimeType,
                    body = text,
                    extraHeaders = headersBuilder {
                        header("cache-control", cacheControl)
                    }
                )
            )
        )
    )
}
