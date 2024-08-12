package com.ustadmobile.ihttp.nanohttpd

import com.ustadmobile.ihttp.response.IHttpResponse
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import kotlinx.io.asInputStream
import java.io.ByteArrayInputStream

fun IHttpResponse.toNanoHttpdResponse() : NanoHTTPD.Response {
    val contentLength = headers["content-length"]?.toLongOrNull()

    return if(contentLength != null) {
        NanoHTTPD.newFixedLengthResponse(
            Status.lookup(responseCode),
            headers["content-type"] ?: "application/octet-stream",
            bodyAsSource()?.asInputStream() ?: ByteArrayInputStream(byteArrayOf()),
            contentLength
        )
    }else {
        NanoHTTPD.newChunkedResponse(
            Status.lookup(responseCode),
            headers["content-type"] ?: "application/octet-stream",
            bodyAsSource()?.asInputStream() ?: ByteArrayInputStream(byteArrayOf())
        )
    }
}
