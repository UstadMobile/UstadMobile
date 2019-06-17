package com.ustadmobile.port.sharedse.networkmanager

import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.util.UMIOUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

/**
 * <h1>ResumableHttpDownload</h1>
 *
 * This class encapsulates HTTP resume logic, it is also responsible to download the file from
 * its source to our application.
 *
 * @author mike
 */
@Deprecated("This is replace with ResumableDownload2 which uses coroutines")
class ResumableHttpDownload(private val httpSrc: String, private val destinationFile: String) {

    private var httpIn: InputStream? = null

    private var fileOut: OutputStream? = null

    private var con: HttpURLConnection? = null

    private val bufferSize = 8 * 1024//8KB

    private val overwriteDestination = true

    /**
     * The total size of the download if known in bytes,or -1 if that's not yet known
     *
     * @return long: Total bytes in a file
     */
    @Volatile
    var totalSize: Long = -1
        private set

    /**
     * The total in bytes downloaded so far. Includes 'resumed' bytes from previous attempts
     *
     * @return long: Total bytes downloaded
     */
    @Volatile
    var downloadedSoFar: Long = 0
        private set

    private val stopped: AtomicInteger

    private val keepAliveEnabled = true

    //map of time (ms) to totalBytesDownloaded at that time to allow calculation of moving average of download speed
    private val downloadProgressHistory = TreeMap<Long, Long>()

    @Volatile
    private var progressHistoryLastRecorded: Long = 0

    /**
     * The minimum amount of time (in ms) after which the progress of a download will be checked and
     * recorded, for use calculating the speed. This is checked during download after filling the
     * buffer. 1000ms by default.
     *
     * @return Interval period for measurement of download progress (in ms)
     */
    /**
     * Setter for progressHistoryInterval
     *
     * @see .getProgressHistoryInterval
     * @param progressHistoryInterval Interval period for measurement of download progress (in ms)
     */
    var progressHistoryInterval = 1000

    /**
     * Calculate the download speed for presentation to the user we use a moving average. This
     * is a First In First Out stack. Essentially the download speed returns the average download
     * speed over (stackSize x historyInterval) ms.
     *
     * @return The stack size for the download progress history tracking. Default 5
     */
    /**
     * Setter for progressHistoryStackSize
     *
     * @see .getProgressHistoryStackSize
     * @param progressHistoryStackSize The stack size for the download progress history tracking.
     */
    var progressHistoryStackSize = 5

    var connectionOpener: URLConnectionOpener? = null

    private val logPrefix: String

    /**
     * Get the http response code (if the response has started)
     *
     * @return HTTP response code (if the ressponse has started)
     */
    var responseCode: Int = 0
        private set

    private val statusLock = ReentrantLock()

    /**
     * Calculate the download speed in bytes per second.
     *
     * @return The current download speed in btyes per second
     */
    //divide delta in byte download by delta time (ms), multiply by 1000 to get speed in bytes per second
    val currentDownloadSpeed: Long
        get() {
            var firstEntry: Map.Entry<Long, Long>
            var lastEntry: Map.Entry<Long, Long>

            synchronized(downloadProgressHistory) {
                if (downloadProgressHistory.size < 2)
                    return 0

                firstEntry = downloadProgressHistory.firstEntry()
                lastEntry = downloadProgressHistory.lastEntry()
            }
            return (lastEntry.value - firstEntry.value) / (lastEntry.key - firstEntry.key) * 1000
        }

    protected val isStopped: Boolean
        get() {
            try {
                statusLock.lock()
                return stopped.get() != 0
            } finally {
                statusLock.unlock()
            }

        }

    init {
        stopped = AtomicInteger(0)
        logPrefix = SUBLOGTAG + "(" + System.identityHashCode(this) + ")"
    }

    /**
     * Method which download the file from its source.
     * @return boolean: TRUE, if the file was downloaded successfully otherwise FALSE.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun download(): Boolean {
        val dlInfoFile = File(destinationFile + DLINFO_EXTENSION)
        val dlPartFile = File(destinationFile + DLPART_EXTENSION)

        var ioe: IOException? = null
        val dlInfoProperties = Properties()
        val url: URL
        var propertiesOut: OutputStream? = null
        var propertiesIn: InputStream? = null


        var completed = false
        try {
            var startFrom = 0L
            val dlPartFileExists = dlPartFile.exists()
            val dlPartFileSize = if (dlPartFileExists) dlPartFile.length() else 0L
            url = URL(httpSrc)

            if (dlPartFile.exists() && dlInfoFile.exists()) {
                propertiesIn = FileInputStream(dlInfoFile)
                dlInfoProperties.load(propertiesIn)
                propertiesIn.close()
                propertiesIn = null
            }

            /**
             * To resume we must have the part download itself, it must have > 0 bytes to be useful,
             * and we must have a last modified date and/or etag to validate the download against
             */
            if (dlPartFileExists && dlPartFileSize > 0 && (dlInfoProperties.containsKey(HTTP_HEADER_LAST_MODIFIED) || dlInfoProperties.containsKey(HTTP_HEADER_ETAG))) {

                if (connectionOpener != null) {
                    con = connectionOpener!!.openConnection(url) as HttpURLConnection
                } else {
                    con = url.openConnection() as HttpURLConnection
                }

                con!!.connectTimeout = HTTP_CONNECT_TIMEOUT
                con!!.readTimeout = HTTP_READ_TIMEOUT
                con!!.requestMethod = "HEAD"
                con!!.setRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_IDENTITY)

                con!!.connect()
                val serverLastModified = con!!.getHeaderField(HTTP_HEADER_LAST_MODIFIED)
                val serverEtag = con!!.getHeaderField(HTTP_HEADER_ETAG)
                httpIn = con!!.inputStream

                httpIn!!.close()
                con!!.disconnect()

                val etagInfoPresent = serverEtag != null && dlInfoProperties.containsKey(HTTP_HEADER_ETAG)
                val lastModifiedInfoPresent = serverLastModified != null && dlInfoProperties.containsKey(HTTP_HEADER_LAST_MODIFIED)
                var validated = etagInfoPresent || lastModifiedInfoPresent

                if (lastModifiedInfoPresent) {
                    validated = validated and (serverLastModified == dlInfoProperties.getProperty(HTTP_HEADER_LAST_MODIFIED))
                }

                if (etagInfoPresent) {
                    validated = validated and (serverEtag == dlInfoProperties.getProperty(HTTP_HEADER_ETAG))
                }

                if (validated) {
                    startFrom = dlPartFile.length()
                    UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + " validated to start from " + startFrom)
                } else {
                    UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + " file exists but not validated")
                }
            }

            if (startFrom == 0L && dlPartFile.exists()) {
                //Part file exists but does not match the last modified and/or etag
                UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + " : startFrom = 0 and dlpart file exists")
                dlPartFile.delete()
            }

            if (connectionOpener != null) {
                con = connectionOpener!!.openConnection(url) as HttpURLConnection
            } else {
                con = url.openConnection() as HttpURLConnection
            }

            con!!.setRequestProperty(HTTP_HEADER_ACCEPT_ENCODING, HTTP_ENCODING_IDENTITY)
            con!!.connectTimeout = HTTP_CONNECT_TIMEOUT
            con!!.readTimeout = HTTP_READ_TIMEOUT

            if (startFrom > 0) {
                con!!.setRequestProperty("Range", "bytes=$dlPartFileSize-")
            }
            con!!.connect()

            //make the dlinfo properties file
            dlInfoProperties.clear()
            if (con!!.getHeaderField(HTTP_HEADER_ETAG) != null)
                dlInfoProperties.setProperty(HTTP_HEADER_ETAG, con!!.getHeaderField(HTTP_HEADER_ETAG))

            if (con!!.getHeaderField(HTTP_HEADER_LAST_MODIFIED) != null)
                dlInfoProperties.setProperty(HTTP_HEADER_LAST_MODIFIED,
                        con!!.getHeaderField(HTTP_HEADER_LAST_MODIFIED))

            var contentRangeResponse: String? = con!!.getHeaderField(HTTP_HEADER_CONTENT_RANGE)

            try {
                statusLock.lock()

                if (contentRangeResponse != null && con!!.responseCode == HttpURLConnection.HTTP_PARTIAL) {
                    contentRangeResponse = contentRangeResponse.substring(contentRangeResponse.indexOf('/') + 1).trim { it <= ' ' }
                    if (contentRangeResponse != "*") {
                        totalSize = java.lang.Long.parseLong(contentRangeResponse)
                    }
                } else {
                    totalSize = con!!.contentLength.toLong()
                }
            } finally {
                statusLock.unlock()
            }

            propertiesOut = FileOutputStream(dlInfoFile)
            dlInfoProperties.store(propertiesOut, "UTF-8")
            propertiesOut.close()
            propertiesOut = null

            responseCode = con!!.responseCode
            val appendToPartFileOutput = responseCode == HttpURLConnection.HTTP_PARTIAL

            try {
                statusLock.lock()

                if (isStopped) {
                    UMLog.l(UMLog.INFO, 0, mkLogPrefix() + " stopped before output file is to be opened")
                    return false
                }

                fileOut = BufferedOutputStream(FileOutputStream(dlPartFile, appendToPartFileOutput))
                downloadedSoFar = if (appendToPartFileOutput) dlPartFileSize else 0L
                progressHistoryLastRecorded = System.currentTimeMillis()
            } finally {
                statusLock.unlock()
            }

            synchronized(downloadProgressHistory) {
                downloadProgressHistory.put(progressHistoryLastRecorded, downloadedSoFar)
            }

            val buf = ByteArray(bufferSize)
            var bytesRead: Int
            httpIn = con!!.inputStream

            var currentTime: Long
            while(httpIn!!.read(buf).also { bytesRead = it } != -1){
                try {
                    statusLock.lock()

                    if (!isStopped) {
                        fileOut!!.write(buf, 0, bytesRead)
                    } else {
                        break
                    }
                } finally {
                    statusLock.unlock()
                }

                currentTime = System.currentTimeMillis()

                try {
                    statusLock.lock()
                    downloadedSoFar += bytesRead.toLong()
                } finally {
                    statusLock.unlock()
                }

                synchronized(downloadProgressHistory) {
                    if (System.currentTimeMillis() - progressHistoryLastRecorded > progressHistoryInterval) {
                        downloadProgressHistory[currentTime] = downloadedSoFar
                        progressHistoryLastRecorded = currentTime

                        if (downloadProgressHistory.size > progressHistoryStackSize)
                            downloadProgressHistory.remove(downloadProgressHistory.firstKey())
                    }
                }
            }
            try {
                statusLock.lock()
                if (!isStopped) {
                    fileOut!!.flush()
                }
                completed = downloadedSoFar == totalSize
            } finally {
                statusLock.unlock()
            }
        } catch (e: IOException) {
            ioe = e
        } finally {
            UMIOUtils.closeInputStream(propertiesIn)
            UMIOUtils.closeOutputStream(propertiesOut)
            UMIOUtils.closeInputStream(httpIn)
            httpIn = null
            try {
                statusLock.lock()
                UMIOUtils.closeOutputStream(fileOut)
                fileOut = null
            } finally {
                statusLock.unlock()
            }

            if (con != null) {
                con!!.disconnect()
                con = null
            }
        }

        if (completed) {
            UMLog.l(UMLog.INFO, 0, mkLogPrefix() + " completed, downloaded " +
                    downloadedSoFar + "bytes")
            synchronized(this) {
                val destinationFile = File(this.destinationFile)
                if (overwriteDestination && destinationFile.exists()) {
                    UMLog.l(UMLog.INFO, 0, mkLogPrefix() +
                            " download complete, overwrite enabled, deleting existing file" +
                            destinationFile.absolutePath)
                    destinationFile.delete()
                }

                if (dlPartFile.renameTo(destinationFile)) {
                    UMLog.l(UMLog.INFO, 0,
                            mkLogPrefix() + " download completed, moved " +
                                    dlInfoFile.absolutePath + " to " + destinationFile.absolutePath)
                } else {
                    throw IllegalStateException(
                            mkLogPrefix() + "Unable to rename completed download part file to destination file from"
                                    + dlPartFile.absolutePath + " to " + destinationFile.absolutePath)
                }

                dlInfoFile.delete()
            }
        }

        UMIOUtils.throwIfNotNullIO(ioe)

        return completed
    }

    /**
     * Stop this download. Everything downloaded so far will be written to disk, and the file will
     * be closed. This method is thread safe, and no reading or writing of the output file will
     * take place after this method returns.
     *
     * @return the number of bytes downloaded
     */
    fun stop(): Long {
        try {
            statusLock.lock()

            stopped.incrementAndGet()

            //stopped.set(true);
            UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + " stop() called")

            //close the file output stream
            if (fileOut != null) {
                try {
                    fileOut!!.flush()
                    UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + "stop: flushed fileout OK")
                } catch (e: IOException) {
                    UMLog.l(UMLog.ERROR, 0, mkLogPrefix() + "stop: exception flushing fileOut", e)
                }

                try {
                    fileOut!!.close()
                    UMLog.l(UMLog.DEBUG, 0, mkLogPrefix() + "stop: closed fileout OK")
                } catch (e: IOException) {
                    UMLog.l(UMLog.ERROR, 0, mkLogPrefix() + "stop: exception closing fileout", e)
                } finally {
                    fileOut = null
                }
            }
        } finally {
            statusLock.unlock()
        }

        return downloadedSoFar
    }

    private fun mkLogPrefix(): String {
        return logPrefix
    }

    companion object {

        private val SUBLOGTAG = "ResumableHttpDownload"

        /**
         * Extension of the file which carry file information
         */
        val DLINFO_EXTENSION = ".dlinfo"

        /**
         * Extension of the partially downloaded file.
         */
        val DLPART_EXTENSION = ".dlpart"

        private val HTTP_HEADER_LAST_MODIFIED = "last-modified"

        private val HTTP_HEADER_ETAG = "etag"

        private val HTTP_HEADER_CONTENT_RANGE = "content-range"

        /**
         * HTTP header accepted encoding type.
         */
        val HTTP_HEADER_ACCEPT_ENCODING = "Accept-Encoding"

        /**
         * HTTP encoding identity
         */
        val HTTP_ENCODING_IDENTITY = "identity"

        /**
         * The timeout to read data. The HttpUrlConnection client on Android by default seems to leave
         * this as being infinite
         */
        private val HTTP_READ_TIMEOUT = 5000

        /**
         * The timeout to connect to an http server. The HttpUrlConnection client on Android by default
         * seems to leave this as being infinite
         */
        private val HTTP_CONNECT_TIMEOUT = 10000
    }
}
