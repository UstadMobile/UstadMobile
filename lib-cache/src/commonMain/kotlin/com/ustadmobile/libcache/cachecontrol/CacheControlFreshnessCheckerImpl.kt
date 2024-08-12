package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.libcache.date.fromHttpDateToMillis

class CacheControlFreshnessCheckerImpl: CacheControlFreshnessChecker {
    override fun invoke(
        requestHeaders: IHttpHeaders,
        requestDirectives: RequestCacheControlHeader?,
        responseHeaders: IHttpHeaders,
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
         *
         * Max-Stale is not used by browsers, but is used by OkHttp when using FORCE_CACHE.
         */
        val requestMaxStale = requestDirectives?.maxStale ?: 0
        val isExplicitlyStale = requestDirectives?.noCache == true ||
                responseDirectives?.let { it.noCache || it.mustRevalidate } == true ||
                requestDirectives?.noCache == true ||
                requestDirectives?.let { requestDir ->
                    requestDir.staleAtAge?.let { age > (it + requestMaxStale)  }
                } == true

        val isExplicitlyFresh = !isExplicitlyStale && (
                requestDirectives?.minFresh?.let { it > (age + requestMaxStale) } == true ||
                        responseDirectives?.immutable == true ||
                        requestDirectives?.maxAge?.let { it > (age + requestMaxStale) } == true ||
                        responseDirectives?.maxAge?.let { it > (age + requestMaxStale) } == true
                )

        val isFresh = if(requestDirectives?.onlyIfCached == true) {
            //Only-if-cached means return the cached response, even if stale, without further validation
            true
        }else if(isExplicitlyStale) {
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