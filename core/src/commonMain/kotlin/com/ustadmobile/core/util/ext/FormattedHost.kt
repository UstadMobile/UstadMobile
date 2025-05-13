package com.ustadmobile.core.util.ext

import io.ktor.http.Url
fun Url.formattedHost(): String {
    return when (protocol.name) {
        "https" -> host
        "http" -> "$this"
        else -> "$this"
    }
}

