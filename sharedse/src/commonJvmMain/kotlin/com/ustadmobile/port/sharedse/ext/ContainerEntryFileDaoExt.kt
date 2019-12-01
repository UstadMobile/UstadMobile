package com.ustadmobile.port.sharedse.ext

import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.io.ConcatenatedPartSource
import com.ustadmobile.core.io.ConcatenatingInputStream
import com.ustadmobile.core.util.ext.hexStringToByteArray
import com.ustadmobile.core.util.ext.toHexString
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest

data class ConcatenatedHttpResponse(val status: Int, val contentLength: Long, val etag: String?,
                                    val lastModifiedTime: Long,
                                    val dataSrc: InputStream?)

fun ContainerEntryFileDao.generateConcatenatedFilesResponse(fileList: String): ConcatenatedHttpResponse {
    val containerEntryFileUids = fileList.split(";").map { it.toLong() }
    val containerEntryFiles = findEntriesByUids(containerEntryFileUids)

    val concatenatedMd5s = mutableListOf<ByteArray>()
    val concatenatedParts = containerEntryFileUids.map { cefUid ->
        val entryFile = containerEntryFiles.firstOrNull { it.cefUid == cefUid  }
        val md5SumVal  = entryFile?.cefMd5
        val entryPathVal = entryFile?.cefPath
        if(entryFile != null && md5SumVal != null && entryPathVal != null) {
            var md5Bytes = md5SumVal.hexStringToByteArray()

            //temporary workaround to handle incorrectly recorded values
            // previous versions of ContainerManager would not pad 0s resulting in md5sum strings
            // that are incorrect and slightly shorter
            if(md5Bytes.size != 16) {
                val resizedArr = ByteArray(16)
                System.arraycopy(md5Bytes, 0, resizedArr, 0, md5Bytes.size)
                md5Bytes = resizedArr
            }

            concatenatedMd5s += md5Bytes
            ConcatenatedPartSource( {FileInputStream(entryPathVal) },  entryFile.ceCompressedSize,
                    entryFile.ceTotalSize, md5Bytes)
        }else {
            null
        }
    }.filter { it != null }.map { it as ConcatenatedPartSource}

    val messageDigest = MessageDigest.getInstance("MD5")
    concatenatedMd5s.forEach { messageDigest.update(it) }
    val etag = messageDigest.digest().toHexString()
    val lastModifiedTime = containerEntryFiles.maxBy { it.lastModified }?.lastModified ?: 0
    return ConcatenatedHttpResponse(200,
            ConcatenatingInputStream.calculateLength(concatenatedParts), etag, lastModifiedTime,
            ConcatenatingInputStream(concatenatedParts))
}
