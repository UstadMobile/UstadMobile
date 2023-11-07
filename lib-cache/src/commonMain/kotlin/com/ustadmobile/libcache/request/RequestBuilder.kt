package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeader
import com.ustadmobile.libcache.headers.HttpHeadersImpl

fun requestBuilder(
    url: String,
    block: RequestBuilder.() -> Unit = { }
) : HttpRequest = requestBuilder {
    this.url = url
    block()
}

fun requestBuilder(
    block: RequestBuilder.() -> Unit
) : HttpRequest {
    return RequestBuilder().also(block).let {
        BaseHttpRequest(
            it.url, HttpHeadersImpl(it.headers), it.method
        )
    }
}


class RequestBuilder internal constructor() {

    var url: String = ""

    var method = HttpRequest.Companion.Method.GET

    internal val headers: MutableList<HttpHeader> = mutableListOf()

    fun header(headerName: String, headerVal: String) {
        headers += HttpHeader(headerName, headerVal)
    }

}
