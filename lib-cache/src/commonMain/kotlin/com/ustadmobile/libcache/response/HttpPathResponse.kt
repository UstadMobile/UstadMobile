package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.headers.lastModifiedHeader
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.atomicfu.atomic
import kotlinx.io.IOException
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class HttpPathResponse(
    private val path: Path,
    private val fileSystem: FileSystem,
    mimeType: String,
    override val request: HttpRequest,
    extraHeaders: HttpHeaders? = null,
): HttpResponse {

    private val bodyRead = atomic(false)

    override val headers: HttpHeaders

    init {
        val metadata = fileSystem.metadataOrNull(path)
            ?: throw IOException("Cannot read from path")
        headers = headersBuilder {
            header("Content-Length", metadata.size.toString())
            header("Content-Type", mimeType)
            header("Accept-Ranges", "bytes")
            if(extraHeaders == null || "last-modified" !in extraHeaders.names()) {
                headersList += lastModifiedHeader()
            }

            extraHeaders?.also { takeFrom(it) }
        }

    }

    override val responseCode: Int
        get() = 200

    override fun bodyAsSource(): Source {
        if(!bodyRead.getAndSet(true)) {
            return fileSystem.source(path).buffered()
        }else {
            throw IllegalStateException("Body has already been read")
        }
    }


}