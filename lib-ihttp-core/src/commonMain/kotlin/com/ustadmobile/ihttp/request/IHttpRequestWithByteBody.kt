package com.ustadmobile.ihttp.request

interface IHttpRequestWithByteBody : IHttpRequest {

    suspend fun bodyAsBytes(): ByteArray?

}