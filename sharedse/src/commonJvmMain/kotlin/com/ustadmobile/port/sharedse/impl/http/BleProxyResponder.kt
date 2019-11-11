package com.ustadmobile.port.sharedse.impl.http

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.nio.charset.Charset

data class BleHttpRequest(var method: String, var uri: String, var headers: Map<String, String>, var postBody: String?) {

}

data class BleHttpResponse(var statusCode: Int, var headers: Map<String, String>, var body: String)

fun NanoHTTPD.IHTTPSession.asBleHttpRequest(): BleHttpRequest {
    return BleHttpRequest(this.method.name, this.uri, this.headers, null)
}

fun NanoHTTPD.Response.asBleHttpResponse(): BleHttpResponse {
    val respClass = NanoHTTPD.Response::class.java
    val headersField = respClass.getField("headers")
    headersField.isAccessible = true
    val headers = headersField.get(this) as Map<String, String>
    return BleHttpResponse(this.status.requestStatus, headers,
            this.data.readBytes().toString(Charset.defaultCharset()))
}

fun BleHttpRequest.asNanoHttpdSession(): NanoHTTPD.IHTTPSession {

}




class BleProxyResponder: RouterNanoHTTPD.UriResponder {
    override fun put(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response {
        val bleAddr  = urlParams?.get("bleAddr")
        var bleRequest = session.asBleHttpRequest()
    }

    override fun other(method: String?, uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun post(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uriResource: RouterNanoHTTPD.UriResource?, urlParams: MutableMap<String, String>?, session: NanoHTTPD.IHTTPSession?): NanoHTTPD.Response {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}