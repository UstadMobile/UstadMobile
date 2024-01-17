package com.ustadmobile.core.domain.contententry.server

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

fun newUnavailableWebResponse(e: Throwable): WebResourceResponse {
    val inStream = ByteArrayInputStream((e.message ?: "").toByteArray())
    return WebResourceResponse(
        "text/plain",
        "utf-8",
        504,
        "Unavailable",
        emptyMap(),
        inStream,
    )
}