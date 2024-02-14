package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceResponse
import io.ktor.http.HttpStatusCode
import okhttp3.Response
import java.io.ByteArrayInputStream

fun Response.asWebResourceResponse(): WebResourceResponse {
    /* Android requires a non-empty HTTP response message, however this was dropped in HTTP2
     *  https://github.com/httpwg/http2-spec/issues/202 .
     *
     * Therefor: if the message is empty, add it using the defaults from KTOR.
     */
    val effectiveMessage = message.ifEmpty {
        HttpStatusCode.fromValue(code).description
    }

    return WebResourceResponse(
        header("content-type") ?: "application/octet-stream",
        "utf-8",
        code,
        effectiveMessage,
        headers.toMap(),
        body?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
    )
}