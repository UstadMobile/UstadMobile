package com.ustadmobile.core.io.ext
import java.io.File
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.util.sanitizeDbNameFromUrl
import okhttp3.internal.closeQuietly
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPInputStream.GZIP_MAGIC

/**
 * Gets a standard subdirectory to use for data that is specific to a given endpoint
 */
fun File.siteDataSubDir(siteEndpoint: Endpoint): File {
    return File(File(this, UstadMobileSystemCommon.SUBDIR_SITEDATA_NAME),
            sanitizeDbNameFromUrl(siteEndpoint.url))
}

fun File.isParentOf(parent: File): Boolean {
    val parentNormalized = parent.normalize()
    var parentToCheck = this.parentFile
    do{
        if(parentToCheck?.normalize() == parentNormalized)
            return true

        parentToCheck = parentToCheck?.parentFile

    }while (parentToCheck != null)

    return false
}

fun makeTempDir(prefix: String, postfix: String = ""): File {
    val tmpDir = File.createTempFile(prefix, postfix)
    return if (tmpDir.delete() && tmpDir.mkdirs())
        tmpDir
    else
        throw IOException("Could not delete / create tmp dir")
}

fun File.getUnCompressedSize(): Long {

    val byteArray = ByteArray(DEFAULT_BUFFER_SIZE)
    var byteCount = 0

    if(isGzipped()) {
        inputStream().use { fileInput ->
            GZIPInputStream(fileInput).use { gzip ->
                while (gzip.read(byteArray).also { byteCount += it } != -1) {

                }
            }
        }
        return byteCount.toLong()
    }
    return length()
}

fun File.isGzipped(): Boolean{
    inputStream().use {
        val signature = ByteArray(2)
        val nread = it.read(signature)
        return nread == 2 && signature[0] == GZIP_MAGIC.toByte() && signature[1] == (GZIP_MAGIC shr 8).toByte()
    }
}