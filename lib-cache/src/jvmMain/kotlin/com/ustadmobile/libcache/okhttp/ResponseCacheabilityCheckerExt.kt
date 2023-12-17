package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.cachecontrol.ResponseCacheabilityChecker
import okhttp3.Response

fun ResponseCacheabilityChecker.canStore(response: Response) : Boolean{
    return this(
        statusCode = response.code,
        responseHeaders = response.headers.asCacheHttpHeaders()
    )
}
