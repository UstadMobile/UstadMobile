package com.ustadmobile.core.embeddedhttp

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.File

fun Response.toHttpdResponse() : NanoHTTPD.Response {
    val contentLength = header("content-length")?.toLong()
    val inStream = body?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
    val status = NanoHTTPD.Response.Status.lookup(code)
    val contentType = header("content-type") ?: "application/octet-stream"
    val response = if(contentLength != null) {
        newFixedLengthResponse(
            status, contentType, inStream, contentLength
        )
    }else {
        NanoHTTPD.newChunkedResponse(status, contentType, inStream)
    }

    headers.names().forEach { headerName ->
        /**
         * NanoHTTPD will set the content-type and content-length headers itself
         */
        if(
            !headerName.equals("content-type", ignoreCase = true) &&
            !headerName.equals("content-length", true)
        ) {
            response.addHeader(headerName, headers[headerName] ?: "")
        }
    }

    return response
}

fun File.toHttpdResponse(
    session: IHTTPSession,
    contentType: String,
) : NanoHTTPD.Response {
    if(!exists()) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", "Not found")
    }

    val etag = Integer.toHexString((name + this.lastModified() + this.length()).hashCode())
    if(session.headers["if-none-match"] == etag) {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, contentType, "")
    }


    val returnInputStream = if(session.method == Method.HEAD) null else this.inputStream()
    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, contentType, returnInputStream, this.length())
}
