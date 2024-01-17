package com.ustadmobile.libcache

import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.HttpResponse

/**
 * @param storageSize the size as stored on disk. If the item was compressed using gzip, then this
 *        is the size after applying compression.
 */
data class StoreResult(
    val urlKey: String,
    val request: HttpRequest,
    val response: HttpResponse,
    val integrity: String,
    val storageSize: Long,
    val lockId: Int = 0,
)
