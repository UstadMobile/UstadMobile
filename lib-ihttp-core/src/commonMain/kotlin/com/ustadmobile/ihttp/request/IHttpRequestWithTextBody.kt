package com.ustadmobile.ihttp.request

interface IHttpRequestWithTextBody: IHttpRequest {

    suspend fun bodyAsText(): String?

}
