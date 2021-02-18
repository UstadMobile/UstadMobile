package com.ustadmobile.core.io.ext


import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.util.ext.encodeBase64
import java.io.OutputStream
import com.ustadmobile.core.db.UmAppDatabase
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

    val rangeResponse: RangeResponse? by lazy {
        requestHeaders.entries.firstOrNull { it.key.toLowerCase()  == "content-range"}?.value
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

fun ContainerEntryFileDao.generateConcatenatedFilesResponse2(md5ListQueryParam: String,
                                                             requestHeaders: Map<String, List<String>> = mapOf(),
                                                             db: UmAppDatabase) : ConcatenatedHttpResponse2 {

    //Convert from hex string to base64 string as used by the database
    val md5List = md5ListQueryParam.split(";").map {
        it.hexStringToByteArray().encodeBase64()
    }

    val containerEntryFiles = findEntriesByMd5SumsSafe(md5List, db)
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
