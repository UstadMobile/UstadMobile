package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeaders

open class BaseHttpRequest(
    override val url: String,
    override val headers: HttpHeaders,
    override val method: HttpRequest.Companion.Method,
): HttpRequest {



}