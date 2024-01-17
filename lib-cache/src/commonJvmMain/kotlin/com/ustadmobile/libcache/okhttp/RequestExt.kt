package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.cachecontrol.RequestCacheControlHeader
import okhttp3.Request

fun Request.mightBeCacheable(
    cacheRequestHeader: RequestCacheControlHeader?
) : Boolean {
    val methodUpper = method.uppercase()
    return (methodUpper == "GET" || methodUpper == "HEAD") && cacheRequestHeader?.noStore != true
}
