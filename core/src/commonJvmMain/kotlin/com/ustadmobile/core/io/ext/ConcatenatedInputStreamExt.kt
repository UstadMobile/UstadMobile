package com.ustadmobile.core.io.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ConcatenatedDataIntegrityException
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.core.io.RangeOutputStream
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherJobHttpUrlConnection2
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.coroutines.coroutineContext
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.github.aakira.napier.Napier
import com.ustadmobile.door.ext.toHexString
import java.io.*
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.ext.base64EncodedToHexString

data class ConcatenatedReadAndSaveResult(val totalBytesRead: Long)

/**
 * Read all the entries from the ConcatenatedInputStream and save them as they come to the given
 * destination directory. As the entries are read they will also be saved to the database. A
 * ContainerEntryFile will be inserted. ContainerEntry(s) will also be generated according
 * to entriesToLink
 *
 * This is used both when downloading files on the client and whilst
 * receiving uploads on the server.
 *
 * @param destDirFile The directory where output is being saved (e.g. where ContainerEntryFiles are saved)
 * @param db UmAppDatabase
 * @param progressAtomicLong An AtomicLong that will be updated as reading progresses.
 * @param md5ExpectedList The expected order of MD5s that will be read. This MUST match the stream itself
 * @param logPrefix prefix to use when logging using Napier
 */
suspend fun ConcatenatedInputStream2.readAndSaveToDir(destDirFile: File,
                                                      db: UmAppDatabase,
                                                      progressAtomicLong: AtomicLong,
                                                      entriesToLink: List<ContainerEntryWithMd5>,
                                                      md5ExpectedList: MutableList<String>,
                                                      logPrefix: String) : ConcatenatedReadAndSaveResult {
    lateinit var concatenatedEntry: ConcatenatedEntry
    val buf = ByteArray(8192)
    var bytesRead = 0
    var totalBytesRead = 0L

    val firstMd5 = md5ExpectedList.first()

    val firstFile = File(destDirFile, "$firstMd5${ContainerFetcherJobHttpUrlConnection2.SUFFIX_PART}")
    val firstFileHeader = File(destDirFile, "$firstMd5${ContainerFetcherJobHttpUrlConnection2.SUFFIX_HEADER}")
    val firstFilePartPresent = firstFile.exists() && firstFileHeader.exists()

    var bytesToSkipWriting = firstFile.length() + firstFileHeader.length()

    while(this.getNextEntry()?.also { concatenatedEntry = it } != null) {
        val entryMd5 = concatenatedEntry.md5.toHexString()
        val nextMd5Expected = md5ExpectedList.removeAt(0)
        if(entryMd5 != nextMd5Expected)
            throw IOException("Server gave us the wrong md5: wanted: $nextMd5Expected / actually got $entryMd5")


        val destFile = File(destDirFile, entryMd5 + ContainerFetcherJobHttpUrlConnection2.SUFFIX_PART)
        val headerFile = File(destDirFile, entryMd5 + ContainerFetcherJobHttpUrlConnection2.SUFFIX_HEADER)
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
            Napier.e("${logPrefix }Data Integrity Exception", die)
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

        val finalDestFile = File(destDirFile, concatenatedEntry.md5.toHexString())
        if(!destFile.renameTo(finalDestFile))
            throw IOException("Could not rename ${destFileOut} to ${finalDestFile}")
        headerFile.delete()

        val containerEntryFile = concatenatedEntry.toContainerEntryFile().apply {
            cefPath = finalDestFile.absolutePath
            cefUid = db.containerEntryFileDao.insertAsync(this)
        }

        val md5Base64 = concatenatedEntry.md5.encodeBase64()
        val entryFiles = entriesToLink.filter { it.cefMd5 == md5Base64 }
        entryFiles.forEach {
            it.ceUid = 0L
            it.ceCefUid = containerEntryFile.cefUid
        }
        db.containerEntryDao.insertListAsync(entryFiles)


    }

    return ConcatenatedReadAndSaveResult(totalBytesRead)
}