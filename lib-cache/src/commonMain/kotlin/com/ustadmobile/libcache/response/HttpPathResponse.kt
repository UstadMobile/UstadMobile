package com.ustadmobile.libcache.response

import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.iHeadersBuilder
import com.ustadmobile.libcache.headers.addIntegrity
import com.ustadmobile.libcache.headers.containsHeader
import com.ustadmobile.libcache.headers.integrity
import com.ustadmobile.libcache.integrity.sha256Integrity
import com.ustadmobile.libcache.io.lastModified
import com.ustadmobile.libcache.io.useAndReadSha256
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
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
    override val request: IHttpRequest,
    integrity: String? = null,
    extraHeaders: IHttpHeaders? = null,
): IHttpResponse {

    private val bodyRead = atomic(false)

    override val headers: IHttpHeaders

    init {
        val metadata = fileSystem.metadataOrNull(path)
            ?: throw IOException("Cannot read from path")
        headers = iHeadersBuilder {
            header("Content-Length", metadata.size.toString())
            header("Content-Type", mimeType)
            header("Accept-Ranges", "bytes")
            if(extraHeaders?.containsHeader("age") != true) {
                header("Age", (systemTimeInMillis() - fileSystem.lastModified(path)).toString())
            }

            val effectiveIntegrity = integrity ?:
                extraHeaders?.integrity() ?:
                sha256Integrity(fileSystem.source(path).buffered().useAndReadSha256())

            extraHeaders?.also { takeFrom(it) }
            addIntegrity(extraHeaders, effectiveIntegrity)
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