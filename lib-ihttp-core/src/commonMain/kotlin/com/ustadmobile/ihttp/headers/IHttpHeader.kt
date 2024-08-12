package com.ustadmobile.ihttp.headers

interface IHttpHeader {

    val name: String

    val value: String

    companion object {

        fun fromNameAndValue(name: String, value: String) = IHttpHeaderImpl(name, value)

        fun fromString(headerLine: String) : IHttpHeader {
            val parts = headerLine.split(':', limit = 2)
            return IHttpHeaderImpl(parts.first().trim(), parts.getOrNull(1)?.trim() ?: "")
        }

    }
}
