package com.ustadmobile.ihttp.okhttp.response

import com.ustadmobile.ihttp.okhttp.headers.asOkHttpHeaders
import com.ustadmobile.ihttp.okhttp.request.asOkHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.asInputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.buffer
import okio.source

fun IHttpResponse.asOkHttpResponse(): Response {
    return if(this is OkHttpResponseAdapter) {
        this.response
    }else {
        val responseMediaType = headers["content-type"]?.toMediaTypeOrNull()
            ?: "application/octet-stream".toMediaType()
        val responseBody = bodyAsSource()?.asInputStream()?.source()
            ?.buffer()?.asResponseBody(
                contentType = responseMediaType,
                contentLength = headers["content-length"]?.toLong() ?: -1
            )

        Response.Builder()
            .headers(headers.asOkHttpHeaders())
            .request(request.asOkHttpRequest())
            .body(responseBody)
            .code(responseCode)
            .protocol(Protocol.HTTP_1_1)
            .message(
                when(responseCode) {
                    206 -> "Partial Content"
                    204 -> "No Content"
                    else -> "OK"
                }
            )
            .build()
    }
}
