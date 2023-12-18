package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.db.entities.ResponseBody
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.headersBuilder
import com.ustadmobile.libcache.io.asKotlinxIoSource
import com.ustadmobile.libcache.io.rangeSource
import com.ustadmobile.libcache.partial.ContentRange
import com.ustadmobile.libcache.partial.RangeRequestNotSatisfiableException
import com.ustadmobile.libcache.request.HttpRequest
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class CacheResponse(
    private val fileSystem: FileSystem,
    override val request: HttpRequest,
    headers: HttpHeaders,
    private val responseBody: ResponseBody,
): HttpResponse {

    @Volatile
    private var httpResponseCode = 0

    override val responseCode: Int
        get() = httpResponseCode

    private val bodyPath = Path(responseBody.storageUri)

    private val rangeResponse: ContentRange?

    override val headers: HttpHeaders

    private val errorBody: ByteArray?

    init {
        val rangeRequestHeader = request.headers["range"]
        var overrideHeaders : HttpHeaders? = null
        var rangeResponse: ContentRange? = null
        var errorBody: ByteArray? = null

        if(rangeRequestHeader != null) {
            val totalSize = fileSystem.metadataOrNull(bodyPath)?.size ?: -1
            try {
                ContentRange.parseRangeHeader(rangeRequestHeader, totalSize).also {
                    rangeResponse = it
                    httpResponseCode = 206
                    overrideHeaders = headersBuilder {
                        takeFrom(headers)
                        header("Content-Length", it.contentLength.toString())
                        header("Content-Range", it.contentRangeResponseHeader)
                    }
                }
            }catch(e: RangeRequestNotSatisfiableException) {
                httpResponseCode = 416
                errorBody = e.message?.toByteArray() ?: byteArrayOf()
                overrideHeaders = headersBuilder {
                    takeFrom(headers)
                    header("Content-Length", errorBody.size.toString())
                }
            }
        }

        this.rangeResponse = rangeResponse
        this.headers = overrideHeaders ?: headers
        this.errorBody = errorBody
    }

    override fun bodyAsSource(): Source {
        return when {
            errorBody != null -> errorBody.asKotlinxIoSource().buffered()
            rangeResponse != null -> {
                fileSystem.rangeSource(bodyPath, rangeResponse.fromByte, rangeResponse.toByte).buffered()
            }
            else -> {
                fileSystem.source(Path(responseBody.storageUri)).buffered()
            }
        }
    }
}