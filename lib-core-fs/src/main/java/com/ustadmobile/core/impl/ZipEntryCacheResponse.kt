package com.ustadmobile.core.impl

import com.ustadmobile.core.impl.http.UmHttpRequest
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMIOUtils

import java.io.File
import java.io.IOException
import java.io.InputStream
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.FileHeader

/**
 * Created by mike on 1/28/18.
 */

class ZipEntryCacheResponse(private val file: File, private val entryPath: String) : AbstractCacheResponse() {

    private var zipEntry: FileHeader? = null

    private var zipFile: ZipFile? = null

    override val responseBody: ByteArray?
        get() {
            if (zipEntry != null) {
                try {
                    return UMIOUtils.readStreamToByteArray(zipFile!!.getInputStream(zipEntry!!))
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: ZipException) {
                    e.printStackTrace()
                }

            }
            return null
        }

    override val responseAsStream: InputStream?
        get() {
            if (zipEntry != null) {
                try {
                    return zipFile!!.getInputStream(zipEntry!!)
                } catch (e: ZipException) {
                    e.printStackTrace()
                }

            }
            return null
        }

    override val isSuccessful: Boolean
        get() = zipEntry != null

    override val status: Int
        get() = if (zipEntry != null) 200 else 404

    override val fileUri: String
        get() = file.absolutePath + "!" + entryPath

    override val isFresh: Boolean
        get() = true

    init {
        try {
            this.zipFile = ZipFile(file)
            this.zipEntry = zipFile!!.getFileHeader(entryPath)
        } catch (e: ZipException) {
            e.printStackTrace()
        }

    }

    override fun getHeader(headerName: String): String? {
        when (headerName) {
            UmHttpRequest.HEADER_CONTENT_TYPE -> return UstadMobileSystemImpl.instance.getMimeTypeFromExtension(
                    UMFileUtil.getExtension(zipEntry!!.fileName)!!)
            UmHttpRequest.HEADER_CONTENT_LENGTH -> return zipEntry!!.uncompressedSize.toString()

            else -> return null
        }
    }

    override fun isFresh(timeToLive: Int): Boolean {
        return true
    }
}
