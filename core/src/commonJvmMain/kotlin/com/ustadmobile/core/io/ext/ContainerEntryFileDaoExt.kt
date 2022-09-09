package com.ustadmobile.core.io.ext


import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.util.ext.encodeBase64
import java.io.OutputStream
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.findEntriesByMd5SumsSafe
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.util.parseRangeRequestHeader
import com.ustadmobile.core.io.ConcatenatedOutputStream2
import com.ustadmobile.door.ext.hexStringToByteArray
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.RangeOutputStream
import com.ustadmobile.lib.util.RangeResponse
import com.ustadmobile.lib.util.sumByLong
import java.io.IOException

val ERROR_PART_NOT_FOUND = 503

class ConcatenatedHttpResponse2(val containerEntryFiles: List<ContainerEntryFile>,
                                requestHeaders: Map<String, List<String>>) {

    //The length of the entire response (before any range is applied)
    val totalLength: Long
        get() = containerEntryFiles.sumByLong { it.ceCompressedSize } +
                (containerEntryFiles.size * ConcatenatedEntry.SIZE)

    /**
     * The actual content length of the response that is going to be sent. This is what should
     * be set as the Content-Length header. This reflects the range if any.
     */
    val actualContentLength: Long by lazy {
        rangeResponse?.let {
            it.actualContentLength
        } ?: totalLength
    }

    val rangeResponse: RangeResponse? by lazy {
        requestHeaders.entries.firstOrNull { it.key.toLowerCase()  == "range"}?.value
                ?.firstOrNull()?.let {
                    parseRangeRequestHeader(it, totalLength)
                }
    }

    val status: Int
        get() = rangeResponse?.statusCode ?: 200

    fun writeTo(dest: OutputStream) {
        val rangeResponseVal = rangeResponse

        val destOut = if(rangeResponseVal != null) {
            RangeOutputStream(dest, rangeResponseVal.fromByte,
                    rangeResponseVal.toByte)
        }else {
            dest
        }

        val concatOut = ConcatenatedOutputStream2(destOut)

        containerEntryFiles.forEach {
            concatOut.putContainerEntryFile(it)
        }

        concatOut.flush()
    }

}

/**
 * @param md5ListQueryParam The list of md5sums that should be concatenated together. These must be
 * encoded in Base64 (e.g. as per ContainerEntryFile.cefMd5).
 * @param requestHeaders Request headers from the http reqquest. Used for supporting partial responses etc.
 * @param db the Database object
 */
fun ContainerEntryFileDao.generateConcatenatedFilesResponse2(md5List: List<String>,
                                                             requestHeaders: Map<String, List<String>> = mapOf(),
                                                             db: UmAppDatabase) : ConcatenatedHttpResponse2 {

    val containerEntryFiles = findEntriesByMd5SumsSafe(md5List)
    val missingMd5s = mutableListOf<String>()
    val entriesToConcatenate = md5List.mapNotNull { md5Str: String ->
        val entry = containerEntryFiles.firstOrNull { it.cefMd5 == md5Str }
        if(entry == null)
            missingMd5s.add(md5Str)

        entry
    }

    if(missingMd5s.isEmpty()) {
        return ConcatenatedHttpResponse2(entriesToConcatenate, requestHeaders)
    }else {
        throw IOException("Container entry not found")
    }
}
