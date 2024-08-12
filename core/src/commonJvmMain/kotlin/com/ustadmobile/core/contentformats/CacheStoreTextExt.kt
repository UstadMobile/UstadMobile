package com.ustadmobile.core.contentformats

import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.ihttp.request.iRequestBuilder
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
    val request = iRequestBuilder(url)
    store(
        storeRequest = listOf(
            CacheEntryToStore(
                request = request,
                response = StringResponse(
                    request = request,
                    mimeType = mimeType,
                    body = text,
                    extraHeaders = iHeadersBuilder {
                        header("cache-control", cacheControl)
                    }
                )
            )
        )
    )
}
