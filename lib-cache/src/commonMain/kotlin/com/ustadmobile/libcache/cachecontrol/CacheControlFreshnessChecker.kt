package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.libcache.headers.HttpHeaders

/**
 * Interface that checks a request and cached response to determine if the response is fresh or stale,
 * and provides validation criteria if required. Provided as an interface for testability purposes
 * (e.g. so it can be mocked for testing other parts of the cache).
 */
interface CacheControlFreshnessChecker {

    /**
     * Determine if a cached response is fresh or stale. Where the response is stale, return the headers
     * that can be used to validate it.
     *
     * @param requestHeaders http request headers
     * @param responseHeaders http headers from the cached response
     * @param responseLastValidated the time the response was generated (would be stored in db, no need
     *        to re-parse) - in millis
     * @param responseFirstStoredTime the time the response was first
     */
    operator fun invoke(
        requestHeaders: HttpHeaders,
        responseHeaders: HttpHeaders,
        responseFirstStoredTime: Long,
        responseLastValidated: Long,
    ) : CachedResponseStatus

}