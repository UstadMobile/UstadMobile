package com.ustadmobile.lib.util

class AcceptEncodingHeader(val acceptableEncodings: Map<String, Float>) {

    /**
     * Check if the given encoding is acceptable according to the header
     */
    fun isEncodingAcceptable(encodingName: String) =
            (acceptableEncodings.get(encodingName) ?: 0F) > 0F || ((acceptableEncodings.get("*") ?: 0F) > 0F)

}

fun parseAcceptedEncoding(acceptEncodingHeader: String?): AcceptEncodingHeader {
    //The default value if the header is not presenter is * as per https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept-Encoding
    val acceptedEncodingsMap = (acceptEncodingHeader ?: "*").split(",")
            .map { it.substringBefore(';').trim() to
                    it.substringAfter(";", "q=1").substringAfter("q=").toFloat()}
            .toMap()
    return AcceptEncodingHeader(acceptedEncodingsMap)

}