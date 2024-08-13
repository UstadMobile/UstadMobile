package com.ustadmobile.ihttp.headers

class IHeadersBuilder internal constructor(
    private val headersList: MutableList<IHttpHeader> = mutableListOf()
) {

    fun takeFrom(headers: IHttpHeaders) {
        headers.names().forEach { name ->
            headersList.removeAll { it.name.equals(name, ignoreCase = true) }
            headers.getAllByName(name).forEach { headerVal ->
                headersList.add(IHttpHeader.fromNameAndValue(name, headerVal))
            }
        }
    }

    fun header(name: String, value: String) {
        headersList.removeAll { it.name.equals(name, true) }
        headersList.add(IHttpHeaderImpl(name, value))
    }

    fun removeHeader(name: String) {
        headersList.removeAll { it.name.equals(name, true) }
    }

    fun build(): IHttpHeaders = HttpHeadersImpl(headersList.toList())

}

fun iHeadersBuilder(
    block: IHeadersBuilder.() -> Unit
): IHttpHeaders {
    return IHeadersBuilder().also(block).build()
}
