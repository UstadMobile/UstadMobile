package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeader

expect fun requestBuilder(
    block: RequestBuilder.() -> Unit
) : HttpRequest

class RequestBuilder internal constructor() {

    var url: String = ""

    var method = HttpRequest.Companion.Method.GET

    internal val headers: MutableList<HttpHeader> = mutableListOf()

    fun header(headerName: String, headerVal: String) {
        headers += HttpHeader(headerName, headerVal)
    }

}
