package com.ustadmobile.libcache.response

import com.ustadmobile.ihttp.headers.IHttpHeaders
import com.ustadmobile.ihttp.headers.MergedHeaders
import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.io.asKotlinxIoSource
import com.ustadmobile.libcache.io.range
import com.ustadmobile.libcache.io.uncompress
import com.ustadmobile.libcache.partial.ContentRange
import com.ustadmobile.libcache.partial.RangeRequestNotSatisfiableException
import com.ustadmobile.ihttp.request.IHttpRequest
import com.ustadmobile.ihttp.response.IHttpResponse
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

class CacheResponse(
    private val fileSystem: FileSystem,
    override val request: IHttpRequest,
    headers: IHttpHeaders,
    private val storageUri: String,
    uncompressedSize: Long,
    @Volatile
    private var httpResponseCode: Int = 200,
): IHttpResponse {

    override val responseCode: Int
        get() = httpResponseCode

    private val rangeResponse: ContentRange?

    override val headers: IHttpHeaders

    private val errorBody: ByteArray?

    //Where the request accept-encoding does not include the content-encoding used to store the body
    //on disk, then we will uncompress the response
    private val bodyUncompressType: CompressionType

    init {
        val rangeRequestHeader = request.headers["range"]
        val ifRangeRequestHeader = request.headers["if-range"]
        val overrideHeadersMap = mutableMapOf<String, List<String>>()

        var rangeResponse: ContentRange? = null
        var errorBody: ByteArray? = null

        //content negotiation
        val acceptEncoding = CompressionType.parseAcceptEncodingHeader(
            request.headers["accept-encoding"])
        val storageContentEncoding = CompressionType.byHeaderVal(
            headers["content-encoding"])

        bodyUncompressType = if(storageContentEncoding !in acceptEncoding) {
            overrideHeadersMap["content-encoding"] = listOf("identity")
            overrideHeadersMap["content-length"] = listOf(uncompressedSize.toString())
            storageContentEncoding
        }else {
            CompressionType.NONE
        }


        if(rangeRequestHeader != null &&
            (ifRangeRequestHeader == null || ifRangeRequestHeader == headers["etag"])
        ) {
            val effectiveContentLength = (overrideHeadersMap["content-length"]?.firstOrNull()
                ?: headers["content-length"])
                ?: throw IllegalStateException("CacheResponse headers missing content-length")
            val totalSize = effectiveContentLength.toLong()

            try {
                ContentRange.parseRangeHeader(rangeRequestHeader, totalSize).also {
                    rangeResponse = it
                    httpResponseCode = 206
                    overrideHeadersMap["content-length"] = listOf(it.contentLength.toString())
                    overrideHeadersMap["Content-Range"] = listOf(it.contentRangeResponseHeader)
                }
            }catch(e: RangeRequestNotSatisfiableException) {
                httpResponseCode = 416
                errorBody = e.message?.toByteArray() ?: byteArrayOf()
                overrideHeadersMap["content-length"] = listOf(errorBody.size.toString())
            }
        }

        this.rangeResponse = rangeResponse
        this.headers = MergedHeaders(
            IHttpHeaders.fromMap(overrideHeadersMap.toMap()), headers
        )
        this.errorBody = errorBody
    }

    override fun bodyAsSource(): Source {
        return when {
            request.method == IHttpRequest.Companion.Method.HEAD -> {
                ByteArray(0).asKotlinxIoSource().buffered()
            }
            errorBody != null -> errorBody.asKotlinxIoSource().buffered()
            else -> {
                val source = fileSystem.source(Path(storageUri)).buffered()
                    .uncompress(bodyUncompressType)
                if(rangeResponse != null) {
                    source.range(rangeResponse.fromByte, rangeResponse.toByte).buffered()
                }else {
                    source
                }
            }
        }
    }
}