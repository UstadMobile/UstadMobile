package com.ustadmobile.libcache.headers

data class HttpHeader(
    val name: String,
    val value: String,
) {
    fun asString() = "$name: $value"

    companion object {

        fun fromString(headerLine: String) : HttpHeader {
            val parts = headerLine.split(':', limit = 2)
            return HttpHeader(parts.first().trim(), parts.getOrNull(1)?.trim() ?: "")
        }

    }
}
