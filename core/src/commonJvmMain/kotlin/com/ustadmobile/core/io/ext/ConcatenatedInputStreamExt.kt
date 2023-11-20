package com.ustadmobile.core.io.ext

import com.ustadmobile.core.io.ConcatenatedDataIntegrityException
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.io.RangeOutputStream
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherOkHttp
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.coroutines.coroutineContext
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.core.io.ConcatenatedEntry
import io.github.aakira.napier.Napier
import java.io.*
import com.ustadmobile.core.util.ext.base64EncodedToHexString
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import kotlinx.serialization.json.Json

data class ConcatenatedReadAndSaveResult(val totalBytesRead: Long)

const val FILE_EXTENSION_CE_JSON = ".ce.json"

/**
 * Read all the entries from the ConcatenatedInputStream and save them as they come to the given
 * destination directory. As the entries are read a json will also be saved into the same directory
 * as md5sum.ce.json. These should be inserted after this function is finished in a transaction
 * along with the ContainerEntry entities (to avoid a ContainerEntryFile being accidentally
 * identified as a Zombie).
 *
 * This is used both when downloading files on the client and whilst
 * receiving uploads on the server.
 *
 * @param destDirFile The directory where output is being saved (e.g. where ContainerEntryFiles are saved)
 * @param tmpDirFile: The directory where work work-in-progress is being saved. This could be the same
 * as destDirFile, or it could be somewhere else. It should be on the same file system so that File.moveTo
 * will work
 *
 * @param db UmAppDatabase
 * @param progressAtomicLong An AtomicLong that will be updated as reading progresses.
 * @param md5ExpectedList The expected order of MD5s that will be read. This MUST match the stream itself.
 * This is a list of Base64 encoded strings.
 * @param logPrefix prefix to use when logging using Napier
 */
@OptIn(ExperimentalStdlibApi::class)
suspend fun ConcatenatedInputStream2.readAndSaveToDir(
    destDirFile: File,
    tmpDirFile: File,
    progressAtomicLong: AtomicLong,
    md5ExpectedList: MutableList<String>,
    logPrefix: String,
    json: Json,
) : ConcatenatedReadAndSaveResult {
    lateinit var concatenatedEntry: ConcatenatedEntry
    val buf = ByteArray(8192)
    var bytesRead = 0
    var totalBytesRead = 0L

    val firstMd5 = md5ExpectedList.first().base64EncodedToHexString()

    val firstFile = File(tmpDirFile, "$firstMd5${ContainerFetcherOkHttp.SUFFIX_PART}")
    val firstFileHeader = File(tmpDirFile, "$firstMd5${ContainerFetcherOkHttp.SUFFIX_HEADER}")
    val firstFilePartPresent = firstFile.exists() && firstFileHeader.exists()

    var bytesToSkipWriting = firstFile.length() + firstFileHeader.length()

    while(this.getNextEntry()?.also { concatenatedEntry = it } != null) {
        val entryMd5 = concatenatedEntry.md5.toHexString()
        val nextMd5Expected = md5ExpectedList.removeAt(0).base64EncodedToHexString()
        if(entryMd5 != nextMd5Expected)
            throw IOException("Server gave us the wrong md5: wanted: $nextMd5Expected / actually got $entryMd5")


        val destFile = File(tmpDirFile, entryMd5 + ContainerFetcherOkHttp.SUFFIX_PART)
        val headerFile = File(tmpDirFile, entryMd5 + ContainerFetcherOkHttp.SUFFIX_HEADER)
        headerFile.writeBytes(concatenatedEntry.toBytes())

        val destFileOut = if(bytesToSkipWriting > 0) {
            //Because we will read through the partially downloaded file, we must use
            //RangeOutputStream to avoid those initial bytes being appended (again) to the
            //file
            RangeOutputStream(FileOutputStream(destFile, true), firstFile.length(), -1L)
        }else {
            FileOutputStream(destFile)
        }

        //Put this to zero so that we don't skip bytes after doing this on the first file
        bytesToSkipWriting = 0

        try {
            while(coroutineContext.isActive && this.read(buf).also { bytesRead = it } != -1) {
                destFileOut.write(buf, 0, bytesRead)
                totalBytesRead += bytesRead
                progressAtomicLong.set(totalBytesRead)
            }
            destFileOut.flush()
        }catch(die: ConcatenatedDataIntegrityException) {
            Napier.e("${logPrefix }Data Integrity Exception - deleting partial file ${destFile.absolutePath}", die)
            destFileOut.close()

            if(!destFile.delete()) {
                Napier.wtf("$logPrefix - could not delete corrupt partial file " +
                        destFile.absolutePath)
            }

            throw die
        }finally {
            destFileOut.close()
        }

        this.verifyCurrentEntryCompleted()

        val md5Hex = concatenatedEntry.md5.toHexString()
        val finalDestFile = File(destDirFile, md5Hex)
        if(!destFile.renameTo(finalDestFile))
            throw IOException("Could not rename ${destFileOut} to ${finalDestFile}")
        headerFile.delete()

        val ceJsonFile = File(destDirFile, "$md5Hex$FILE_EXTENSION_CE_JSON")
        val containerEntryFile = concatenatedEntry.toContainerEntryFile().apply {
            cefPath = finalDestFile.absolutePath
        }
        ceJsonFile.writeText(Json.encodeToString(
            ContainerEntryFile.serializer(), containerEntryFile))
    }

    return ConcatenatedReadAndSaveResult(totalBytesRead)
}