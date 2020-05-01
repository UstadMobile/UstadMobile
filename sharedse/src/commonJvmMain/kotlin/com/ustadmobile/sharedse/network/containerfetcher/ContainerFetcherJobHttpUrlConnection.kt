package com.ustadmobile.sharedse.network.containerfetcher

import com.ustadmobile.core.db.JobStatus
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import com.ustadmobile.sharedse.network.NetworkManagerWithConnectionOpener
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.content
import com.github.aakira.napier.Napier


typealias ConnectionOpener = (url: URL) -> HttpURLConnection

class ContainerDownloaderJobHttpUrlConnection(val request: ContainerFetcherRequest,
                                              val listener: ContainerFetcherListener? = null,
                                              val networkManager: NetworkManagerBle,
                                              val extras: Map<Int, Any> = mapOf()) {

    private val totalDownloadSize = AtomicLong(0L)

    private val bytesSoFar = AtomicLong(0L)

    var responseHeaders: Map<String?, List<String?>>? = null
        private set

    var responseCode: Int = -1
        private set

    suspend fun progressUpdater() = coroutineScope {
        while(isActive) {
            listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
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

            val destinationFile = File(request.fileDest)

            var startFrom = 0L
            val dlInfoFile = File(request.fileDest + DLINFO_EXTENSION)
            val dlPartFile = File(request.fileDest + DLPART_EXTENSION)
            val dlInfoMap = mutableMapOf<String, String?>()



            try {
                val localConnectionOpenerVal =
                        (networkManager as NetworkManagerWithConnectionOpener).localConnectionOpener

                val connectionOpener: ConnectionOpener = if(localConnectionOpenerVal != null) {
                    localConnectionOpenerVal
                }else {
                    {url -> url.openConnection() as HttpURLConnection}
                }

                if (dlPartFile.exists() && dlInfoFile.exists()) {
                    Json.parse(JsonObject.serializer(), dlInfoFile.readText()).forEach {
                        dlInfoMap[it.key.toLowerCase()] = it.value.content
                    }
                }


                if (dlInfoMap.any { it.key in VALIDATION_HEADERS }) {
                    val headConnection = connectionOpener(URL(request.url))
                    headConnection.setRequestMethod("HEAD")

                    val headResponseHeaders: Map<String?, List<String?>> = headConnection.headerFields
                    val validated = headResponseHeaders.filter { it.key?.toLowerCase() in VALIDATION_HEADERS }
                            .any {
                                val headerLowerCase = it.key?.toLowerCase() ?: return@any false
                                dlInfoMap[headerLowerCase] == it.value.firstOrNull()
                            }
                    val headContentLength = headConnection.getHeaderField("Content-Length")
                    headConnection.disconnect()

                    if(validated) {
                        startFrom = dlPartFile.length()
                        Napier.d("Validated download to start from $startFrom bytes")
                        if(headContentLength != null)
                            totalDownloadSize.set(headContentLength.toLong())

                    }else {
                        Napier.d("File exists but not validated")
                    }
                }

                urlConnection = connectionOpener(URL(request.url))
                if(startFrom > 0L) {
                    urlConnection.setRequestProperty("Content-Range", "bytes=$startFrom-")
                }

                inStream = urlConnection.inputStream

                dlInfoMap.clear()
                val getResponseHeaders: Map<String?, List<String?>> = urlConnection.headerFields
                getResponseHeaders.filter { it.key?.toLowerCase() in VALIDATION_HEADERS }.forEach {
                    val headerLowerCase = it.key?.toLowerCase() ?: return@forEach
                    dlInfoMap[headerLowerCase] = it.value.firstOrNull()
                }
                responseHeaders = getResponseHeaders
                responseCode = urlConnection.responseCode

                val jsonObj = JsonObject(dlInfoMap.map { entry -> Pair(entry.key, JsonPrimitive(entry.value)) }.toMap())
                dlInfoFile.writeText(Json.stringify(JsonObject.serializer(), jsonObj))

                val isPartialResponse: Boolean = urlConnection.responseCode == 206
                fileOut = FileOutputStream(dlPartFile, isPartialResponse)

            val contentLengthField = urlConnection.getHeaderField("Content-Length")
                if(responseCode == 200 && contentLengthField != null) {
                    totalDownloadSize.set(contentLengthField.toLong())
                }

                val buf = ByteArray(8192)
                var bytesRead = 0
                var totalBytesRead = if(isPartialResponse) dlPartFile.length() else 0L
                while(isActive && inStream.read(buf).also { bytesRead = it } != -1) {
                    fileOut.write(buf, 0, bytesRead)
                    totalBytesRead += bytesRead
                    bytesSoFar.set(totalBytesRead)
                }
                fileOut.flush()
                fileOut.close()

                downloadStatus = if(isActive && totalBytesRead == totalDownloadSize.get()) {
                    dlPartFile.renameTo(destinationFile)
                    JobStatus.COMPLETE
                } else {
                    JobStatus.PAUSED
                }

                Napier.d({"Container download done in ${System.currentTimeMillis() - startTime}"})
            }finally {
                progressUpdaterJob.cancel()
                listener?.onProgress(request, bytesSoFar.get(), totalDownloadSize.get())
                inStream?.close()
                fileOut?.close()
                inStream?.close()
            }

            downloadStatus
        }

    }

    companion object {

        const val DLINFO_EXTENSION = ".dlinfo"

        /**
         * Extension of the partially downloaded file.
         */
        const val DLPART_EXTENSION = ".dlpart"

        private const val HTTP_HEADER_LAST_MODIFIED = "last-modified"

        private const val HTTP_HEADER_ETAG = "etag"

        private const val HTTP_HEADER_CONTENT_RANGE = "content-range"

        val VALIDATION_HEADERS = listOf(HTTP_HEADER_ETAG, HTTP_HEADER_LAST_MODIFIED)


    }

}