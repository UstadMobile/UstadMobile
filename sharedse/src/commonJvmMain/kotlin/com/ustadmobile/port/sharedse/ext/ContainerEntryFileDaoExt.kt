package com.ustadmobile.port.sharedse.ext

import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.sharedse.io.ConcatenatedPartSource
import com.ustadmobile.sharedse.io.ConcatenatingInputStream
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.util.ext.encodeBase64
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.io.ByteArrayInputStream
import com.github.aakira.napier.Napier
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.serialization.toUtf8Bytes
import com.ustadmobile.port.sharedse.impl.http.RangeInputStream
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.core.io.ConcatenatedOutputStream2
import com.ustadmobile.core.io.ext.putContainerEntryFile

data class ConcatenatedHttpResponse(val status: Int, val contentLength: Long, val etag: String?,
                                    val lastModifiedTime: Long,
                                    val dataSrc: InputStream?,
                                    val responseHeaders: Map<String, String> = mapOf())

val ERROR_PART_NOT_FOUND = 503

class ConcatenatedHttpResponse2(val status: Int, val contentLength: Long, val etag: String?,
                                     val lastModifiedTime: Long,
                                     val responseHeaders: Map<String, String> = mapOf(),
                                     private val containerEntryFiles: List<ContainerEntryFile>) {

    fun writeTo(dest: OutputStream) {
        val concatOut = ConcatenatedOutputStream2(dest)
        containerEntryFiles.forEach {
            concatOut.putContainerEntryFile(it)
        }
        concatOut.flush()
    }

}

fun ContainerEntryFileDao.generateConcatenatedFilesResponse2(md5ListQueryParam: String,
        method: String = "GET",
        requestHeaders: Map<String, List<String>> = mapOf(),
        db: UmAppDatabase) : ConcatenatedHttpResponse {

    val md5List = md5ListQueryParam.split(";")

    val containerEntryFiles = findEntriesByMd5SumsSafe(md5List, db)

    TODO("Not yet")
}

@Deprecated("Use generateConcatenatedFilesResponse2.")
fun ContainerEntryFileDao.generateConcatenatedFilesResponse(fileList: String,
                                                            method: String = "GET",
                                                            requestHeaders: Map<String, List<String>> = mapOf()): ConcatenatedHttpResponse {
    val containerEntryFileUids = fileList.split(";").map { it.toLong() }
    val containerEntryFiles = findEntriesByUids(containerEntryFileUids)

    val concatenatedMd5s = mutableListOf<ByteArray>()
    val missingParts = mutableListOf<Long>()
    val concatenatedParts = containerEntryFileUids.map { cefUid ->
        val entryFile = containerEntryFiles.firstOrNull { it.cefUid == cefUid  }
        val md5SumVal  = entryFile?.cefMd5
        val entryPathVal = entryFile?.cefPath
        if(entryFile != null && md5SumVal != null && entryPathVal != null) {
            val md5Bytes = md5SumVal.base64StringToByteArray()
            concatenatedMd5s += md5Bytes
            ConcatenatedPartSource( {FileInputStream(entryPathVal) },  entryFile.ceCompressedSize,
                    entryFile.ceTotalSize, md5Bytes)
        }else {
            missingParts.add(cefUid)
            null
        }
    }.filter { it != null }.map { it as ConcatenatedPartSource }

    if(concatenatedParts.size != containerEntryFileUids.size) {
        val errorMessageBytes = "Missing parts: ${missingParts.joinToString()}".toUtf8Bytes()
        return ConcatenatedHttpResponse(ERROR_PART_NOT_FOUND, errorMessageBytes.size.toLong(), null,
                0, ByteArrayInputStream(errorMessageBytes))
    }else {
        Napier.d("Concatenation request contained ${containerEntryFileUids.size} file uids")
        Napier.d("Concatenating ${concatenatedParts.size} entries: ${concatenatedParts.joinToString {it.partId.encodeBase64()} }")

        val messageDigest = MessageDigest.getInstance("MD5")
        concatenatedMd5s.forEach { messageDigest.update(it) }
        val etag = messageDigest.digest().encodeBase64()
        val lastModifiedTime = containerEntryFiles.maxBy { it.lastModified }?.lastModified ?: 0

        val rangeRequestHeader = requestHeaders.entries
                .firstOrNull { it.key.toLowerCase()  == "content-range"}?.value?.firstOrNull()
        val totalLength = ConcatenatingInputStream.calculateLength(concatenatedParts)
        val concatenatingInputStream = if(method.equals("HEAD", true)) {
            null
        }else {
            ConcatenatingInputStream(concatenatedParts)
        }

        if(rangeRequestHeader != null) {
            val rangeResponse = parseRangeRequestHeader(rangeRequestHeader, totalLength)
            val rangeInputStream = if(concatenatingInputStream != null) {
                RangeInputStream(concatenatingInputStream, rangeResponse.fromByte,
                        rangeResponse.toByte)
            }else {
                null
            }

            return ConcatenatedHttpResponse(206, rangeResponse.actualContentLength, etag,
                    lastModifiedTime, rangeInputStream)
        }else {
            return ConcatenatedHttpResponse(200, totalLength, etag, lastModifiedTime,
                    concatenatingInputStream)
        }


    }


}
