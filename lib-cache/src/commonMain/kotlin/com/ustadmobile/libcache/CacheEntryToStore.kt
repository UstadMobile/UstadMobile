package com.ustadmobile.libcache

import com.ustadmobile.libcache.request.HttpRequest
import com.ustadmobile.libcache.response.HttpResponse
import kotlinx.io.files.Path

/**
 * Store a request and response in the cache.
 *
 * @param request HttpRequest
 * @param response HttpResponse to store
 * @param responseBodyTmpLocalPath a local (e.g. file) URI where the body is stored in a temporary
 *        file that can be moved into the cache directory (to avoid the need to copy).
 * @param skipChecksumIfProvided if true and the checksum is already provided on the response header, then
 *        running a checksum will be skipped. When responseBodyTmpLocalUri is set and skipChecksum
 *        is true, then reading the body can be avoided.
 */
data class CacheEntryToStore(
    val request: HttpRequest,
    val response: HttpResponse,
    val responseBodyTmpLocalPath: Path? = null,
    val skipChecksumIfProvided: Boolean = false,
)

