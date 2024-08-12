package com.ustadmobile.ihttp.headers


/**
 * Turn headers into a string separated by CRLF as per the HTTP spec
 */
fun IHttpHeaders.asString() : String{
    return names().flatMap {name ->
        getAllByName(name).map { IHttpHeader.fromNameAndValue(name, it) }
    }.joinToString(separator = "\r\n") { it.asString() }
}

fun IHttpHeaders.contentLength(): Long? {
    return this["content-length"]?.toLong()
}
