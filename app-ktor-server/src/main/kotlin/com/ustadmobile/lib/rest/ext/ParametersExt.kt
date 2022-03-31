package com.ustadmobile.lib.rest.ext

import io.ktor.http.*
import io.ktor.util.*
import java.net.URLEncoder

fun Parameters.toQueryParamString() : String {
    val queryParamPairs = toMap().entries.map { mapEntry ->
        mapEntry.value.map { mapEntry.key to it }
    }.flatten()

    return if (queryParamPairs.isNotEmpty()) {
        "?" + queryParamPairs.joinToString(separator = "&") {
            URLEncoder.encode(it.first, "UTF-8") + "=" + URLEncoder.encode(it.second, "UTF-8")
        }
    }else {
        ""
    }
}
