package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceResponse
import okhttp3.Response
import java.io.ByteArrayInputStream

fun Response.asWebResourceResponse(): WebResourceResponse {
    return WebResourceResponse(
        header("content-type") ?: "application/octet-stream",
        "utf-8",
        code,
        message,
        headers.toMap(),
        body?.byteStream() ?: ByteArrayInputStream(ByteArray(0))
    )
}