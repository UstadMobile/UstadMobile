package com.ustadmobile.ihttp.headers

interface IHttpHeaders {

    operator fun get(name: String): String?

    fun getAllByName(name: String): List<String>

    fun names(): Set<String>

    companion object {

        private val EMPTY_HEADERS = HttpHeadersImpl(emptyList())

        fun fromString(headersString: String): IHttpHeaders {
            return HttpHeadersImpl(
                headers = headersString.split("\r\n").map {
                    IHttpHeader.fromString(it)
                }
            )
        }

        fun fromMap(map: Map<String, List<String>>): IHttpHeaders {
            return MapHttpHeadersAdapter(map)
        }


        fun emptyHeaders() = EMPTY_HEADERS

    }

}