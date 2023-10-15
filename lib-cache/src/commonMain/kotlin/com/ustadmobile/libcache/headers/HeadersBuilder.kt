package com.ustadmobile.libcache.headers

class HeadersBuilder internal constructor(
    val headersList: MutableList<HttpHeader> = mutableListOf()
) {

    fun takeFrom(headers: HttpHeaders) {
        headers.names().forEach { name ->
            headersList.removeAll { it.name.equals(name, ignoreCase = true) }
            headers.getAllByName(name).forEach { headerVal ->
                headersList.add(HttpHeader(name, headerVal))
            }
        }
    }

    fun header(name: String, value: String) {
        headersList.removeAll { it.name.equals(name, false) }
        headersList.add(HttpHeader(name, value))
    }

    fun build(): HttpHeaders = HttpHeadersImpl(headersList.toList())

}

fun headersBuilder(
    block: HeadersBuilder.() -> Unit
): HttpHeaders {
    return HeadersBuilder().also(block).build()
}
