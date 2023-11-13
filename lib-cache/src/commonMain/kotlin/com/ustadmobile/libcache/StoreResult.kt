package com.ustadmobile.libcache

import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.HttpResponse

data class StoreResult(
    val request: HttpRequest,
    val response: HttpResponse,
    val lockId: Int = 0,
)

