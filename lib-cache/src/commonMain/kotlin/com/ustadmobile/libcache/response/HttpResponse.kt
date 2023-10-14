package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest

interface HttpResponse {

    val request: HttpRequest

    val headers: HttpHeaders

}

