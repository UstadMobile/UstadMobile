package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeaders

abstract class AbstractHttpRequest(
    override val url: String,
    override val headers: HttpHeaders,
    override val method: HttpRequest.Companion.Method,
): HttpRequest {



}