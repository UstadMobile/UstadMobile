package com.ustadmobile.ihttp.request

import com.ustadmobile.ihttp.headers.HttpHeadersImpl
import com.ustadmobile.ihttp.headers.IHttpHeader

fun iRequestBuilder(
    url: String,
    block: RequestBuilder.() -> Unit = { }
) : IHttpRequest = requestBuilder {
    this.url = url
    block()
}

fun requestBuilder(
    block: RequestBuilder.() -> Unit
) : IHttpRequest {
    return RequestBuilder().also(block).let {
        BaseHttpRequest(
            it.url, HttpHeadersImpl(it.headers), it.method, body = it.body
        )
    }
}


class RequestBuilder internal constructor() {

    var url: String = ""

    var method = IHttpRequest.Companion.Method.GET

    internal var body: ByteArray? = null

    internal val headers: MutableList<IHttpHeader> = mutableListOf()

    fun header(headerName: String, headerVal: String) {
        headers += IHttpHeader.fromNameAndValue(headerName, headerVal)
    }

    fun body(byteArray: ByteArray){
        body = byteArray
    }

    fun body(bodyText: String) {
        body = bodyText.encodeToByteArray()
    }

}
