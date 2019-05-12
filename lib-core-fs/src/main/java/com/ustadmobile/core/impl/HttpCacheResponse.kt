package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.impl.http.UmHttpResponse
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.HttpCachedEntry

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

/**
 * Represents an Http Cache Response. There are three modes of delivering a response:
 * 1. If the data was already cached and the entry was fresh or validated by a 304 not modified:
 * Data is served directly from the file the cache entry was stored in.
 *
 * 2. If the request was performed asynchronously then the cache response object will save the data
 * to disk and retain the buffer (so it need not be read back from the disk). This is performed
 * by saveNetworkResponseToDiskAndBuffer .
 *
 * 3. If the request was performed synchronously then initPipe should be called to setup piped input
 * and output streams, and then pipeNetworkResponseToDisk should be called in a separate thread
 * (eg. using an ExecutorService). The executor service will ensure the data is promptly written
 * to disk regardless of whether or not the consumer which made the request processes it promptly.
 *
 *
 * Created by mike on 12/27/17.
 */

class HttpCacheResponse(val entry: HttpCachedEntry, val request: UmHttpRequest) : AbstractCacheResponse(), Runnable {

    private var bufferPipeIn: PipedInputStream? = null

    private var bufferedPipeOut: PipedOutputStream? = null

    var networkResponse: UmHttpResponse? = null

    internal var bodyReturned = false

    internal var responseCompleteListener: ResponseCompleteListener? = null

    private val maxPipeBuffer = 2 * 1024 * 1024

    private var byteBuf: ByteArray? = null

    override val responseBody: ByteArray?
        get() {
            if (!hasResponseBody())
                try {
                    throw IOException("getResponseBody called on response that has no body")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            markBodyReturned()
            if (networkResponse == null) {
                try {
                    return UMIOUtils.readStreamToByteArray(FileInputStream(File(entry.fileUri)))
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            } else if (byteBuf != null) {
                return byteBuf
            } else {
                try {
                    return UMIOUtils.readStreamToByteArray(bufferPipeIn!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return null
        }

    override val responseAsStream: InputStream?
        get() {
            if (!hasResponseBody())
                try {
                    throw IOException("getResponseAsStream called on response that has no body")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            markBodyReturned()
            if (networkResponse == null) {
                try {
                    return FileInputStream(File(entry.fileUri))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            } else return if (byteBuf != null) {
                ByteArrayInputStream(byteBuf!!)
            } else {
                bufferPipeIn
            }
            return null
        }

    override val isSuccessful: Boolean
        get() = entry.statusCode >= 200 && entry.statusCode < 400

    override val status: Int
        get() = entry.statusCode

    override val fileUri: String
        get() = entry.fileUri

    override val isFresh: Boolean
        get() = HttpCache.isFresh(entry)


    internal interface ResponseCompleteListener {
        fun onResponseComplete(response: HttpCacheResponse)
    }

    init {
        cacheResponse = AbstractCacheResponse.MISS
    }

    override fun run() {
        pipeNetworkResponseToDisk()
    }

    fun initPipe() {
        val networkLengthHeader = networkResponse!!.getHeader(UmHttpRequest.HEADER_CONTENT_LENGTH)
        val networkEncodingHeader = networkResponse!!.getHeader(UmHttpRequest.HEADER_CONTENT_ENCODING)

        //if the content-length is provided and gzip encoding is not being used, then the maximum
        //pipe size we need is content-length.
        var pipeSize = maxPipeBuffer
        if (networkLengthHeader != null && (networkEncodingHeader == null || networkEncodingHeader == "identity")) {
            try {
                pipeSize = Math.min(maxPipeBuffer, Integer.parseInt(networkLengthHeader))
            } catch (e: NumberFormatException) {
                UstadMobileSystemImpl.l(UMLog.ERROR, 0, networkLengthHeader, e)
            }

        }

        bufferPipeIn = PipedInputStream(pipeSize)

        try {
            bufferedPipeOut = PipedOutputStream(bufferPipeIn)
        } catch (e: IOException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0,
                    "HttpCacheResponse: Exception with pipe init")
        }

    }

    protected fun pipeNetworkResponseToDisk() {
        var networkIn: InputStream? = null
        var fout: FileOutputStream? = null
        var responseCompleted = false
        try {
            networkIn = networkResponse!!.responseAsStream
            fout = FileOutputStream(entry.fileUri)

            val buf = ByteArray(8 * 1024)
            var bytesRead: Int
            while ((bytesRead = networkIn!!.read(buf)) != -1) {
                bufferedPipeOut!!.write(buf, 0, bytesRead)
                fout.write(buf, 0, bytesRead)
            }

            fout.flush()
            bufferedPipeOut!!.flush()
            responseCompleted = true
        } catch (e: IOException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Exception piping cache response to disk", e)
        } finally {
            UMIOUtils.closeInputStream(networkIn)
            UMIOUtils.closeOutputStream(fout)
            UMIOUtils.closeOutputStream(bufferedPipeOut)
        }

        if (responseCompleted && responseCompleteListener != null)
            responseCompleteListener!!.onResponseComplete(this)
    }

    fun saveNetworkResponseToDiskAndBuffer() {
        var fout: FileOutputStream? = null
        var responseCompleted = false
        try {
            byteBuf = networkResponse!!.responseBody
            fout = FileOutputStream(entry.fileUri)
            fout.write(byteBuf!!)
            fout.flush()
            responseCompleted = true
        } catch (e: IOException) {
            UstadMobileSystemImpl.l(UMLog.ERROR, 0, "Exception writing / buffering response", e)
        } finally {
            UMIOUtils.closeOutputStream(fout)
        }

        if (responseCompleted && responseCompleteListener != null)
            responseCompleteListener!!.onResponseComplete(this)
    }


    override fun getHeader(headerName: String): String? {
        var headerName = headerName
        headerName = headerName.toLowerCase()
        when (headerName) {
            UmHttpRequest.HEADER_CACHE_CONTROL -> return entry.cacheControl
            UmHttpRequest.HEADER_CONTENT_LENGTH -> return entry.contentLength.toString()
            UmHttpRequest.HEADER_CONTENT_TYPE -> return entry.contentType
            UmHttpRequest.HEADER_ETAG -> return entry.etag
            UmHttpRequest.HEADER_EXPIRES -> return UMCalendarUtil.makeHTTPDate(entry.expiresTime)

            else -> return null
        }
    }

    private fun markBodyReturned() {
        if (bodyReturned)
            throw IllegalStateException("HttpCacheResponse: Body already returned")

        bodyReturned = true
    }

    fun setOnResponseCompleteListener(responseCompleteListener: ResponseCompleteListener) {
        this.responseCompleteListener = responseCompleteListener
    }

    override fun isFresh(timeToLive: Int): Boolean {
        return HttpCache.isFresh(entry, timeToLive)
    }

    /**
     * Single point to determine if this response has a request body.
     *
     * @return true if there is an http body attached with this request, false otherwise.
     */
    protected fun hasResponseBody(): Boolean {
        return UmHttpRequest.METHOD_HEAD != request.getMethod() && entry.statusCode != 204
    }
}
