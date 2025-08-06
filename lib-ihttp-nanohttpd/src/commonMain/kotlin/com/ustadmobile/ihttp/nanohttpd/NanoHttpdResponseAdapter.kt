package com.ustadmobile.ihttp.nanohttpd

import com.ustadmobile.ihttp.response.IHttpResponse
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status
import kotlinx.io.asInputStream
import java.io.ByteArrayInputStream

fun IHttpResponse.toNanoHttpdResponse() : NanoHTTPD.Response {
    val contentLength = headers["content-length"]?.toLongOrNull()

    val response = if(contentLength != null) {
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

    headers.names().forEach { headerName ->
        /**
         * NanoHTTPD will set the content-type and content-length headers itself
         */
        headers.getAllByName(headerName)
            .filter {
                !it.equals("content-type", true) &&
                    !it.equals("content-length", true)
            }.forEach { headerVal ->
                response.addHeader(headerName, headerVal)
            }
    }

    return response
}
