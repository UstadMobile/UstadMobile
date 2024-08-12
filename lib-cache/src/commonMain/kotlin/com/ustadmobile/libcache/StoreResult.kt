package com.ustadmobile.libcache

import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse

/**
 * @param storageSize the size as stored on disk. If the item was compressed using gzip, then this
 *        is the size after applying compression.
 */
data class StoreResult(
    val urlKey: String,
    val request: IHttpRequest,
    val response: IHttpResponse,
    val integrity: String,
    val storageSize: Long,
    val lockId: Long = 0,
)
