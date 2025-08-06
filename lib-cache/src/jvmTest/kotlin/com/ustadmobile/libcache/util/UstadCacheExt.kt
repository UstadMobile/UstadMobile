package com.ustadmobile.libcache.util

import com.ustadmobile.ihttp.headers.IHttpHeader
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.request.requestBuilder
import com.ustadmobile.ihttp.response.IHttpResponse
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.StoreResult
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.response.HttpPathResponse
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File

data class FileStoredAsUrl(
    val request: IHttpRequest,
    val response: IHttpResponse,
    val storeResult: StoreResult,
    val file: File,
)

fun UstadCache.storeFileAsUrl(
    testFile: File,
    testUrl: String,
    mimeType: String,
    requestHeaders: List<IHttpHeader> = emptyList(),
): FileStoredAsUrl {
    val request = requestBuilder {
        url = testUrl
        requestHeaders.forEach {
            header(it.name, it.value)
        }
    }

    val response = HttpPathResponse(
        path = Path(testFile.absolutePath),
        fileSystem = SystemFileSystem,
        mimeType = mimeType,
        request = request,
    )

    val storeResult = store(
        listOf(
            CacheEntryToStore(
                request = request,
                response = response,
            )
        ),
    )

    return FileStoredAsUrl(request, response, storeResult.first(), testFile)
}