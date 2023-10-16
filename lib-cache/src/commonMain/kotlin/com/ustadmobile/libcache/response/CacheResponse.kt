package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class CacheResponse(
    private val fileSystem: FileSystem,
    override val request: HttpRequest,
    override val headers: HttpHeaders,
    private val responseBody: ResponseBody,
): HttpResponse {

    override fun bodyAsSource(): Source {
        return fileSystem.source(Path(responseBody.storageUri)).buffered()
    }
}