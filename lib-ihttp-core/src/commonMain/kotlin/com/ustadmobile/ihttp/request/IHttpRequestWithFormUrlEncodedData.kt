package com.ustadmobile.ihttp.request

interface IHttpRequestWithFormUrlEncodedData: IHttpRequest {

    suspend fun bodyAsFormUrlEncodedDataMap(): Map<String, List<String>>

}