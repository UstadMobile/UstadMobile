package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils
import java.io.*

/**
 * Represents a cache 'response' that is in fact simply a wrapper for a file. This is used so that
 * the ImageLoader class can use the HttpCache, and thus load images from http or the file system
 */
class FileProtocolCacheResponse(private val file: File) : AbstractCacheResponse() {

    override val responseBody: ByteArray?
        get() {
            try {
                return UMIOUtils.readStreamToByteArray(FileInputStream(file))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

    override val responseAsStream: InputStream?
        get() {
            try {
                return FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            return null
        }

    override val isSuccessful: Boolean
        get() = true

    override val status: Int
        get() = 200

    override val fileUri: String
        get() = file.absolutePath

    override val isFresh: Boolean
        get() = true

    override fun getHeader(headerName: String): String? {
        when (headerName) {
            UmHttpRequest.HEADER_CONTENT_TYPE -> return UstadMobileSystemImpl.instance.getMimeTypeFromExtension(
                    UMFileUtil.getExtension(file.name)!!)
            UmHttpRequest.HEADER_CONTENT_LENGTH -> return file.length().toString()

            else -> return null
        }
    }

    override fun isFresh(timeToLive: Int): Boolean {
        return true
    }
}
