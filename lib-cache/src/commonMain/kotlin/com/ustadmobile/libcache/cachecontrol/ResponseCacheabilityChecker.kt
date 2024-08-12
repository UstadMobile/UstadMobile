package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.ihttp.headers.IHttpHeaders


/**
 * Determine if the given response can be stored. Responses that can be cached must:
 *
 * 1) Have a 200 status code
 * 3) Not have no-store in the cache-control response headers
 */
interface ResponseCacheabilityChecker {

    operator fun invoke(
        statusCode: Int,
        responseHeaders: IHttpHeaders,
        responseCacheDirectives: ResponseCacheControlHeader? = responseHeaders["cache-control"]?.let {
            ResponseCacheControlHeader.parse(it)
        },
        acceptPartialContent: Boolean = false,
    ): Boolean

}