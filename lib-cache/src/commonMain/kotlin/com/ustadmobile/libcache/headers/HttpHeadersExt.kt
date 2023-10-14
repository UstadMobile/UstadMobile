package com.ustadmobile.libcache.headers

fun HttpHeaders.toHeadersList(): List<HttpHeader> {
    return if(this is HttpHeadersImpl) {
        this.headers
    }else {
        this.names().flatMap { headerName ->
            this.getAllByName(headerName).map { HttpHeader(headerName, it) }
        }
    }
}