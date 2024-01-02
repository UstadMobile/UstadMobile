package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.libcache.headers.HttpHeaders

class ResponseCacheabilityCheckerImpl: ResponseCacheabilityChecker {
    override fun invoke(
        statusCode: Int,
        responseHeaders: HttpHeaders,
        responseCacheDirectives: ResponseCacheControlHeader?
    ): Boolean {
        //Partial status, no content, etc is not cacheable
        if(statusCode != 200)
            return false

        //Cannot
        if(responseCacheDirectives?.noStore == true)
            return false

        return true
    }
}