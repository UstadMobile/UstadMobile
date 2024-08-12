package com.ustadmobile.libcache.okhttp

import com.ustadmobile.ihttp.okhttp.headers.asIHttpHeaders
import com.ustadmobile.libcache.cachecontrol.ResponseCacheabilityChecker
import okhttp3.Response
import okhttp3.internal.http.promisesBody

fun ResponseCacheabilityChecker.canStore(
    response: Response,
    acceptPartialResponse: Boolean = false,
) : Boolean{
    return response.promisesBody() && this(
        statusCode = response.code,
        responseHeaders = response.headers.asIHttpHeaders(),
        acceptPartialContent = acceptPartialResponse,
    )
}
