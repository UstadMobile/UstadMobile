package com.ustadmobile.sharedse.network.fetch

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*


typealias ProgressListener = (requestId: Int, btyesDownloaded: Long, totalBytes: Long) -> Unit

class ProgressResponseBody(private val requestId: Int,
                           private val src: ResponseBody,
                           private val listener: ProgressListener): ResponseBody() {

    private val bufferedSource: BufferedSource by lazy {
        Okio.buffer(source(src.source()))
    }

    override fun contentLength() = src.contentLength()

    override fun contentType() = src.contentType()

    override fun source(): BufferedSource = bufferedSource

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            internal var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0L
                listener(requestId, totalBytesRead, src.contentLength())
                return bytesRead
            }
        }
    }


}