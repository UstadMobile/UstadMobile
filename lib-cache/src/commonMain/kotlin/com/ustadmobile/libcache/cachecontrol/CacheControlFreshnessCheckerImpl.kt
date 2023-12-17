package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.libcache.date.fromHttpDateToMillis
import com.ustadmobile.libcache.headers.HttpHeaders

class CacheControlFreshnessCheckerImpl: CacheControlFreshnessChecker {
    override fun invoke(
        requestHeaders: HttpHeaders,
        requestDirectives: RequestCacheControlHeader?,
        responseHeaders: HttpHeaders,
        responseDirectives: ResponseCacheControlHeader?,
        responseFirstStoredTime: Long,
        responseLastValidated: Long
    ): CachedResponseStatus {
        //When the response is revalidated, the age header should also be updated
        val ageHeader = (responseHeaders["age"]?.toLong() ?: 0)

        val age = ((systemTimeInMillis() - responseLastValidated) / 1000) + ageHeader

        /**
         * As per https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching#common_caching_patterns
         * if the request contains a "no-cache" directive, it can still be validated.
         *
         * In case of conflicting directives, the response will be considered stale.
         */
        val isExplicitlyStale = requestDirectives?.noCache == true ||
                responseDirectives?.let { it.noCache || it.mustRevalidate } == true ||
                requestDirectives?.maxAge?.let { age > it } == true ||
                responseDirectives?.maxAge?.let {  maxAge ->
                    age > maxAge && !(requestDirectives?.minFresh?.let { it > age } == true)
                } == true

        val isExplicitlyFresh = !isExplicitlyStale && (
                requestDirectives?.minFresh?.let { it > age } == true ||
                        responseDirectives?.immutable == true ||
                        requestDirectives?.maxAge?.let { it > age } == true ||
                        responseDirectives?.maxAge?.let { it > age } == true

                )

        val isFresh = if(isExplicitlyStale) {
            false
        }else if(isExplicitlyFresh) {
            true
        }else {
            //Heuristic
            val timeLastModified = (responseHeaders["last-modified"]?.fromHttpDateToMillis()
                ?: responseFirstStoredTime)
            val timeSinceModified = (responseLastValidated - timeLastModified)
            (timeLastModified + (timeSinceModified * HEURISTIC_VALIDITY_FACTOR)) < systemTimeInMillis()
        }


        return CachedResponseStatus(
            isFresh = isFresh,
            ifNoneMatch = if(!isFresh) responseHeaders["etag"] else null,
            ifNotModifiedSince = if(!isFresh) responseHeaders["last-modified"] else null
        )
    }

    companion object {

        const val HEURISTIC_VALIDITY_FACTOR = 1.1f
    }
}