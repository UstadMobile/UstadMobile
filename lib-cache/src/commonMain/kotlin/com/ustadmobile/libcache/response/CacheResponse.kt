package com.ustadmobile.libcache.response

import com.ustadmobile.libcache.CompressionType
import com.ustadmobile.libcache.headers.HttpHeaders
import com.ustadmobile.libcache.headers.MergedHeaders
import com.ustadmobile.libcache.io.asKotlinxIoSource
import com.ustadmobile.libcache.io.range
import com.ustadmobile.libcache.io.uncompress
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
    private val storageUri: String,
    uncompressedSize: Long,
    @Volatile
    private var httpResponseCode: Int = 200,
): HttpResponse {

    override val responseCode: Int
        get() = httpResponseCode

    private val bodyPath = Path(storageUri)

    private val rangeResponse: ContentRange?

    override val headers: HttpHeaders

    private val errorBody: ByteArray?

    //Where the request accept-encoding does not include the content-encoding used to store the body
    //on disk, then we will uncompress the response
    private val bodyUncompressType: CompressionType

    init {
        val rangeRequestHeader = request.headers["range"]
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


        if(rangeRequestHeader != null) {
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
            HttpHeaders.fromMap(overrideHeadersMap.toMap()), headers
        )
        this.errorBody = errorBody
    }

    override fun bodyAsSource(): Source {
        return when {
            request.method == HttpRequest.Companion.Method.HEAD -> {
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