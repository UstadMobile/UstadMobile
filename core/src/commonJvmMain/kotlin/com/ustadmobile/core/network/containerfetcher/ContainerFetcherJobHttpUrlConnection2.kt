package com.ustadmobile.core.network.containerfetcher

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao
import com.ustadmobile.core.io.ConcatenatedEntry
import com.ustadmobile.core.io.ConcatenatedInputStream2
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.doorIdentityHashCode
import kotlinx.coroutines.*
import org.kodein.di.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.core.io.ext.parseKmpUriStringToFile
import com.ustadmobile.core.io.ext.readAndSaveToDir
import com.ustadmobile.door.ext.toHexString
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.core.util.ext.base64EncodedToHexString

class ContainerFetcherJobHttpUrlConnection2(val request: ContainerFetcherRequest2,
                                            val listener: ContainerFetcherListener2?,
                                            override val di: DI): DIAware {

    private val totalDownloadSize = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    private val db: UmAppDatabase by di.on(Endpoint(request.siteUrl)).instance(tag = DoorTag.TAG_DB)

    private var startTime: Long = 0

    private fun HttpURLConnection.requireContentLength(): Long {
        val headerVal = getHeaderField("content-length")
                ?: throw IllegalStateException("requireContentLength: no content-length header")
        return headerVal.toLong()
    }

    private val logPrefix: String by lazy {
        "ContainerDownloaderJobHttpUrlConnection2 @${this.doorIdentityHashCode}"
    }

    suspend fun progressUpdater() = coroutineScope {
        while(isActive) {
            listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
            delay(500L)
        }
    }

    suspend fun download(): Int {
        val progressUpdaterJob = GlobalScope.async(Dispatchers.Default) {
            progressUpdater()
        }

        startTime = System.currentTimeMillis()
        var downloadStatus = 0
        var urlConnection: HttpURLConnection? = null
        var inStream: ConcatenatedInputStream2? = null

        //We always download in md5sum (hex) alphabetical order, such that a partial download will
        //be resumed as expected.
        val md5sToDownload = request.entriesToDownload.mapNotNull {
            it.cefMd5?.base64EncodedToHexString()
        }.toSet().toList().sorted()

        val md5ListString = md5sToDownload.joinToString(separator = ";")
        val md5ExpectedList = md5sToDownload.toMutableList()
        val firstMd5 = md5sToDownload.first()
        val destDirFile = request.destDirUri.parseKmpUriStringToFile()


        //TODO: Avoid duplicating this
        val firstFile = File(destDirFile, "$firstMd5$SUFFIX_PART")
        val firstFileHeader = File(destDirFile, "$firstMd5$SUFFIX_HEADER")
        val firstFilePartPresent = firstFile.exists() && firstFileHeader.exists()

        try {
            //check and see if the first file is already here
            val inputUrl = "${request.mirrorUrl}/${ContainerEntryFileDao.ENDPOINT_CONCATENATEDFILES2}/$md5ListString"
            Napier.d("$logPrefix Download ${md5sToDownload.size} container files $inputUrl -> ${request.destDirUri}")
            val localConnectionOpener : LocalURLConnectionOpener? = di.direct.instanceOrNull()
            val url = URL(inputUrl)
            urlConnection = localConnectionOpener?.openLocalConnection(url)
                    ?: url.openConnection() as HttpURLConnection

            if(firstFilePartPresent) {
                val startFrom = firstFile.length() + firstFileHeader.length()
                Napier.d("$logPrefix partial download from $startFrom")
                urlConnection.addRequestProperty("range", "bytes=${startFrom}-")
            }


            inStream = ConcatenatedInputStream2(if(firstFilePartPresent) {
                //If the first file exists, we must read the contents of it's header, then the payload,
                //so that the checuksum will match

                //checking if this might be causing an issue due to reading from a file that is appended to...
                val inputStreamList = listOf(FileInputStream(firstFileHeader),
                        FileInputStream(firstFile), urlConnection.inputStream)

                SequenceInputStream(Collections.enumeration(inputStreamList))
            }else {
                urlConnection.inputStream
            })

            val bytesToSkipWriting = firstFile.length() + firstFileHeader.length()
            totalDownloadSize.set((firstFile.length() + firstFileHeader.length()) + urlConnection.requireContentLength())

            val readAndSaveResult = inStream.readAndSaveToDir(destDirFile, db, bytesSoFar,
                    request.entriesToDownload, md5ExpectedList, logPrefix)
            val totalBytesRead= readAndSaveResult.totalBytesRead
            val payloadExpected = (totalDownloadSize.get() - (md5sToDownload.size * ConcatenatedEntry.SIZE))

            downloadStatus = if(totalBytesRead == payloadExpected) {
                JobStatus.COMPLETE
            }else {
                JobStatus.PAUSED
            }
            Napier.d("$logPrefix done downloaded ${bytesSoFar.get() - bytesToSkipWriting}/expected ${payloadExpected} bytes" +
                    " in ${System.currentTimeMillis() - startTime}ms")
        }catch(e: Exception) {
            Napier.e("$logPrefix exception downloading", e)
        }finally {
            inStream?.close()
            progressUpdaterJob.cancel()
            listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
        }

        return downloadStatus
    }

    companion object {

        const val SUFFIX_PART = ".part"

        const val SUFFIX_HEADER = ".header"

    }

}