package com.ustadmobile.ihttp.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.request.IHttpRequest
import kotlinx.io.Source

interface IHttpResponse {

    val responseCode: Int

    val request: IHttpRequest

    val headers: IHttpHeaders

    fun bodyAsSource(): Source?

}

