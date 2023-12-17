package com.ustadmobile.libcache.okhttp

import com.ustadmobile.libcache.cachecontrol.RequestCacheControlHeader
import okhttp3.Request

fun Request.mightBeCacheable(
    cacheRequestHeader: RequestCacheControlHeader?
) : Boolean {
    return method.uppercase() == "GET" && !(cacheRequestHeader?.noStore == true)
}
