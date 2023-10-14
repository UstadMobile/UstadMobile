package com.ustadmobile.libcache.request

import com.ustadmobile.libcache.headers.HttpHeaders
import java.io.InputStream

interface OutgoingBody {

    val size: Long

    val inStream: InputStream

    val contentType: String

}

class HttpRequestJvm(
    url: String,
    headers: HttpHeaders,
    method: HttpRequest.Companion.Method,
    val body: OutgoingBody? = null
): AbstractHttpRequest(
    url = url,
    headers = headers,
    method = method
)
