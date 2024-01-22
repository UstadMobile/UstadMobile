package com.ustadmobile.libcache.headers

interface HttpHeaders {

    operator fun get(name: String): String?

    fun getAllByName(name: String): List<String>

    fun names(): Set<String>

    companion object {

        private val EMPTY_HEADERS = HttpHeadersImpl(emptyList())

        fun fromString(headersString: String): HttpHeaders {
            return HttpHeadersImpl(
                headers = headersString.split("\r\n").map {
                    HttpHeader.fromString(it)
                }
            )
        }

        fun fromMap(map: Map<String, List<String>>): HttpHeaders {
            return MapHttpHeadersAdapter(map)
        }


        fun emptyHeaders() = EMPTY_HEADERS

    }

}