package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source

interface HttpResponse {

    val responseCode: Int

    val request: HttpRequest

    val headers: HttpHeaders

    fun bodyAsSource(): Source?

}

