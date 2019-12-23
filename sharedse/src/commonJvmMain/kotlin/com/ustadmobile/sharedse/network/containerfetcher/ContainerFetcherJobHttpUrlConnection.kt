package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.core.db.JobStatus
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import com.github.aakira.napier.Napier
import com.ustadmobile.sharedse.network.NetworkManagerWithConnectionOpener
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.sharedse.network.NetworkManagerBle

typealias ConnectionOpener = (url: URL) -> HttpURLConnection

class ContainerDownloaderJobHttpUrlConnection(val request: ContainerFetcherRequest,
                                              val listener: ContainerFetcherListener? = null,
                                              val networkManager: NetworkManagerBle,
                                              val extras: Map<Int, Any> = mapOf()) {

    private val contentLength = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    suspend fun progressUpdater() = coroutineScope {
        while(isActive) {
            listener?.onProgress(request, bytesSoFar.get(), contentLength.get())
            delay(500L)
        }
    }

    suspend fun download(): Int {
        return coroutineScope {
            val progressUpdaterJob = async { progressUpdater() }
            val startTime = System.currentTimeMillis()
            var downloadStatus = 0

            var urlConnection: HttpURLConnection? = null
            var inStream: InputStream? = null
            var fileOut: FileOutputStream? = null
            try {
                val localConnectionOpenerVal =
                        (networkManager as NetworkManagerWithConnectionOpener).localConnectionOpener

                val connectionOpener: ConnectionOpener = if(localConnectionOpenerVal != null) {
                    localConnectionOpenerVal
                }else {
                    {url -> url.openConnection() as HttpURLConnection}
                }

                urlConnection = connectionOpener(URL(request.url))
                val contentLengthField = urlConnection.getHeaderField("Content-Length")
                if(contentLength != null) {
                    contentLength.set(contentLengthField.toLong())
                }

                inStream = urlConnection.inputStream
                fileOut = FileOutputStream(request.fileDest)

                val buf = ByteArray(8192)
                var bytesRead = 0
                var totalBytesRead = 0L
                while(isActive && inStream.read(buf).also { bytesRead = it } != -1) {
                    fileOut.write(buf, 0, bytesRead)
                    totalBytesRead += bytesRead
                    bytesSoFar.set(totalBytesRead)
                }
                fileOut.flush()

                downloadStatus = if(isActive) {
                    JobStatus.COMPLETE
                } else {
                    JobStatus.PAUSED
                }

                Napier.d({"Container download done in ${System.currentTimeMillis() - startTime}"})
            }finally {
                progressUpdaterJob.cancel()
                listener?.onProgress(request, bytesSoFar.get(), contentLength.get())
                inStream?.close()
                fileOut?.close()
                inStream?.close()
            }

            downloadStatus
        }

    }

    companion object {

        const val EXTRAID_CONNECTION_OPENER = 1

    }

}