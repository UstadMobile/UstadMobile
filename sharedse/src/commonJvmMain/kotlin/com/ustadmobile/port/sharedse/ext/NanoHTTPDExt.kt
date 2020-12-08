package com.ustadmobile.port.sharedse.ext

import fi.iki.elonen.NanoHTTPD
import java.lang.reflect.Field
import java.io.InputStream
import java.util.zip.GZIPInputStream

fun newUnsupportedMethodResponse(): NanoHTTPD.Response {
    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
            "text/plain", "Method not allowed")
}


private val headersField: Field by lazy {
    NanoHTTPD.Response::class.java.getDeclaredField("header").also {
        it.isAccessible = true
    }
}

/**
 * Use reflection to get the header map for a NanoHTTPD.Response object
 */
val NanoHTTPD.Response.responseHeaders: Map<String, String>
    get() {
        return headersField.get(this) as MutableMap<String, String>
    }

/**
 * If the response is Gzip encoded (as per the Content-Encoding header), provide an input stream
 * wrapped with GZIPInputStream toinflate data. Otherwise return the original input stream.
 */
fun NanoHTTPD.Response.dataInflatedIfRequired(): InputStream{
    val gzipHeader = getHeader("Content-Encoding")
    return if(gzipHeader == "gzip") {
        GZIPInputStream(data)
    }else {
        data
    }
}
