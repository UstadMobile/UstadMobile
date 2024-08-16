package com.ustadmobile.core.util.rawhttp

import com.ustadmobile.httpoveripc.core.SimpleTextResponse
import rawhttp.core.RawHttp
import rawhttp.core.RawHttpResponse
import rawhttp.core.body.StringBody

fun RawHttp.newRawHttpStringResponse(
    statusCode: Int,
    body: String,
    contentType: String = "text/plain"
) : RawHttpResponse<*>{
    return parseResponse(
        "HTTP/1.1 $statusCode ${SimpleTextResponse.STATUS_RESPONSES[statusCode] ?: ""}\n" +
                "Content-Type: $contentType").withBody(StringBody(body))
}
