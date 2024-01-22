package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.libcache.headers.HttpHeaders

/**
 * Determine if the given response can be stored. Responses that can be cached must:
 *
 * 1) Have a 200 status code
 * 3) Not have no-store in the cache-control response headers
 */
interface ResponseCacheabilityChecker {

    operator fun invoke(
        statusCode: Int,
        responseHeaders: HttpHeaders,
        responseCacheDirectives: ResponseCacheControlHeader? = responseHeaders["cache-control"]?.let {
            ResponseCacheControlHeader.parse(it)
        }
    ): Boolean

}