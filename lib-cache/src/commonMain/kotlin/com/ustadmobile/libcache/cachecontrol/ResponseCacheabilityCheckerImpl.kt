package com.ustadmobile.libcache.cachecontrol

import com.ustadmobile.ihttp.headers.IHttpHeaders

class ResponseCacheabilityCheckerImpl: ResponseCacheabilityChecker {
    override fun invoke(
        statusCode: Int,
        responseHeaders: IHttpHeaders,
        responseCacheDirectives: ResponseCacheControlHeader?,
        acceptPartialContent: Boolean
    ): Boolean {
        //Do not store error responses. Only store partial content responses when explicitly expected
        // in response to our own directives
        if(!(statusCode == 200 || (statusCode == 206 && acceptPartialContent)))
            return false

        //do not store a response if the cache directives expressly tell us no
        return responseCacheDirectives?.noStore != true
    }
}