package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeadersImpl


actual fun requestBuilder(
    block: RequestBuilder.() -> Unit
) : HttpRequest {
    return RequestBuilder().also(block).let {
        HttpRequestJvm(
            it.url, HttpHeadersImpl(it.headers), it.method
        )
    }
}
