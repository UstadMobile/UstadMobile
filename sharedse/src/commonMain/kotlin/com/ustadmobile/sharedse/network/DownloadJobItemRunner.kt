package com.ustadmobile.sharedse.network

import com.github.aakira.napier.Napier
import com.tonyodev.fetch2.Error
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerEntryFileDao.Companion.ENDPOINT_CONCATENATEDFILES
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.io.ConcatenatedInputStream
import com.ustadmobile.core.io.ConcatenatedInputStreamEntrySource
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_DISCONNECTED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_CLOUD
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_LOCAL
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.sumByLong
import com.ustadmobile.sharedse.io.FileInputStreamSe
import com.ustadmobile.sharedse.io.FileSe
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import com.ustadmobile.sharedse.network.fetch.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import com.ustadmobile.core.util.ext.encodeBase64
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext
import kotlin.jvm.Volatile

/**
 * Class which handles all file downloading tasks, it reacts to different status as changed
 * in the Db from either UI or Network change.
 *
 * i.e Decides where to get the file based on the entry status response,
 * connecting to the peer device via BLE and WiFiP2P for the actual download
 * and Change its status based on Network status.
 *
 * @author kileha3
 */
class DownloadJobItemRunner
/**
 * Constructor to be used when creating new instance of the runner.
 * @param downloadItem Item to be downloaded
 * @param networkManager BLE network manager for network operation controls.
 * @param appDb Application database instance
 * @param endpointUrl Endpoint to get the file from.
 * @param mainCoroutineDispatcher A coroutine dispatcher that will, on Android, dispatch on the main
 * thread. This is required because Room's LiveData.observeForever must be called from the main thread
 */
(private val context: Any, private val downloadItem: DownloadJobItem,
 private val networkManager: NetworkManagerBleCommon, private val appDb: UmAppDatabase,
 private val appDbRepo: UmAppDatabase,
 private val endpointUrl: String, private var connectivityStatus: ConnectivityStatus?,
 private val retryDelay: Long = 3000,
 private val mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
 private val numConcurrentEntryDownloads: Int = 4,
 private val localAvailabilityManager: LocalAvailabilityManager,
 private val ioCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    private lateinit var downloadJobItemManager: DownloadJobItemManager

    private var statusLiveData: DoorLiveData<ConnectivityStatus?>? = null

    private var statusObserver: DoorObserver<ConnectivityStatus?>? = null

    private var downloadJobItemObserver: DoorObserver<Int>? = null

    private var downloadJobItemLiveData: DoorLiveData<Int>? = null

    private var downloadSetConnectivityData: DoorLiveData<Boolean>? = null

    private var downloadSetConnectivityObserver: DoorObserver<Boolean>? = null

    private val completedEntriesBytesDownloaded = atomic(0L)

    private val runnerStatus = atomic(JobStatus.NOT_QUEUED)

    private val meteredConnectionAllowed = atomic(-1)

    private var lWiFiConnectionTimeout = 30000

    private val wiFiDirectGroupBle = atomic<WiFiDirectGroupBle?>(null)

    private var currentNetworkNode: NetworkNode? = null

    private lateinit var currentHttpClient: HttpClient

    /**
     * Boolean to indicate if we are waiting for a local connection.
     */
    private val waitingForLocalConnection = atomic(false)

    private val downloadWiFiLock = Any()

    private var destinationDir: String? = null

    //private val numFailures = atomic(0)

    private val entriesDownloaded = atomic(0)

    private val connectionRequestActive = atomic(false)

    private val statusRef = atomic<ConnectivityStatus?>(null)

    val startDownloadFnJobRef = atomic<Job?>(null)

//    private val inProgressDownloadCounters = AtomicLongArray(numConcurrentEntryDownloads)

//    private val currentFetchRequests = copyOnWriteListOf<RequestMpp>()
//
//    private val currentFetchDownloads = mutableMapOf<Int, DownloadMpp>()
//
//    private val fetchStartTimes = mutableMapOf<Int, Long>()
//
//    private var numCompletedOrFailed = 0

    private val currentDownloadAttempt = atomic(null as CompletableDeferred<Int>?)

    private var existingEntriesBytesDownloaded = 0L

    @Volatile
    private var currentFetchRequestId: Int = -1

    private var currentFetchDownload: DownloadMpp? = null

    private val fetchListener = object: AbstractFetchListenerMpp() {

        override fun onAdded(download: DownloadMpp) {
            if(download.id == currentFetchRequestId) {
                currentFetchDownload = download
                Napier.d({"Download added #${download.id} ${download.url}"})
            }
        }

        override fun onCompleted(download: DownloadMpp) {
            if(download.id == currentFetchRequestId) {
                Napier.d({"Download completed #${download.id} ${download.url}"})
                currentDownloadAttempt.value?.complete(JobStatus.COMPLETE)
            }
        }

        override fun onError(download: DownloadMpp, error: Error, throwable: Throwable?) {
            if(download.id == currentFetchRequestId) {
                Napier.d({"Download error #${download.id} ${download.url}"}, throwable)
                currentDownloadAttempt.value?.completeExceptionally(throwable ?: IOException("$error"))
            }
        }

        override fun onProgress(download: DownloadMpp, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            if(download.id == currentFetchRequestId) {
                Napier.d({"Download progress #${download.id} ${download.url} - ${download.downloaded}bytes"})
                GlobalScope.launch {
                    downloadJobItemManager.updateProgress(downloadItem.djiUid, download.downloaded,
                            downloadItem.downloadLength)
                }
            }
        }
    }

    fun setWiFiConnectionTimeout(lWiFiConnectionTimeout: Int) {
        this.lWiFiConnectionTimeout = lWiFiConnectionTimeout
    }


    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    private fun handleConnectivityStatusChanged(newStatus: ConnectivityStatus?) {
        this.connectivityStatus = newStatus
        UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() +
                " Connectivity state changed: " + newStatus)
        if (waitingForLocalConnection.value)
            return

        if (connectivityStatus != null) {
            when (newStatus!!.connectivityState) {
                STATE_METERED -> if (meteredConnectionAllowed.value == 0) {
                    GlobalScope.launch { stop(JobStatus.WAITING_FOR_CONNECTION, cancel = true) }
                }

                STATE_DISCONNECTED -> GlobalScope.launch { stop(JobStatus.WAITING_FOR_CONNECTION, cancel = true) }
            }//TODO: check CONNECTING_LOCAL - if the status changed, but we are not the job that asked for that
        }
    }

    /**
     * Handle changes triggered when Download set metered connection flag changes
     * @param meteredConnection changed metered connection flag.
     */
    private fun handleDownloadSetMeteredConnectionAllowedChanged(meteredConnection: Boolean?) {
        if (meteredConnection != null) {
            if (meteredConnection) {
                meteredConnectionAllowed.value = 1
            } else {
                meteredConnectionAllowed.value = 0
            }

            if (meteredConnectionAllowed.value == 0 && connectivityStatus != null
                    && connectivityStatus!!.connectivityState == STATE_METERED) {
                UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() + " : no longer allowed to run on metered network - stopping")
                GlobalScope.launch { stop(JobStatus.WAITING_FOR_CONNECTION, cancel = true) }
            }
        }
    }

    /**
     * Handle changes triggered when the download job item status changes
     * @param newDownloadStatus changed download job item status
     */

    private fun handleDownloadJobItemStatusChanged(newDownloadStatus: Int) {
        if (newDownloadStatus == JobStatus.STOPPING) {
            GlobalScope.launch { stop(JobStatus.STOPPED, cancel = true) }
        }
    }

    /**
     * Stop the download task from continuing (if not already stopped). Calling stop for a second
     * time will have no effect.
     *
     * @param newStatus new status to be set
     */
    suspend fun stop(newStatus: Int, cancel: Boolean = false) {
        if (!runnerStatus.compareAndSet(JobStatus.STOPPED, JobStatus.STOPPED)) {
            if (cancel) {
                val startDownloadFnJob = startDownloadFnJobRef.value
                println("===CANCELLING $startDownloadFnJob===")
                startDownloadFnJob?.cancel()
            }

            networkManager.httpFetcher.removeListener(fetchListener)

            //TODO: if download is active, pause it here


            withContext(mainCoroutineDispatcher) {
                statusLiveData!!.removeObserver(statusObserver!!)
                downloadJobItemLiveData!!.removeObserver(downloadJobItemObserver!!)
                downloadSetConnectivityData!!.removeObserver(downloadSetConnectivityObserver!!)
            }

            updateItemStatus(newStatus)
            networkManager.releaseWifiLock(downloadWiFiLock)
        }

    }


    suspend fun download() {
        downloadJobItemManager = networkManager.openDownloadJobItemManager(downloadItem.djiDjUid)!!
        println("Download started for  ${downloadItem.djiDjUid}")
        runnerStatus.value = JobStatus.RUNNING
        updateItemStatus(JobStatus.RUNNING)
        val downloadJobId = downloadItem.djiDjUid
        appDb.downloadJobDao.updateStatus(downloadJobId, JobStatus.RUNNING)

        statusLiveData = appDb.connectivityStatusDao.statusLive()
        downloadJobItemLiveData = appDb.downloadJobItemDao.getLiveStatus(downloadItem.djiUid)

        //get the download set
        downloadSetConnectivityData = appDb.downloadJobDao.getLiveMeteredNetworkAllowed(downloadJobId)

        downloadSetConnectivityObserver = object : DoorObserver<Boolean> {
            override fun onChanged(t: Boolean) {
                handleDownloadSetMeteredConnectionAllowedChanged(t)
            }
        }

        statusObserver = ObserverFnWrapper(this::handleConnectivityStatusChanged)
        downloadJobItemObserver = ObserverFnWrapper(this::handleDownloadJobItemStatusChanged)

        withContext(mainCoroutineDispatcher) {
            statusLiveData!!.observeForever(statusObserver!!)
            downloadJobItemLiveData!!.observeForever(downloadJobItemObserver!!)
            downloadSetConnectivityData!!.observeForever(downloadSetConnectivityObserver!!)
        }

        networkManager.httpFetcher.addListener(fetchListener)

        destinationDir = appDb.downloadJobDao.getDestinationDir(downloadJobId)
        if (destinationDir == null) {
            val e = IllegalArgumentException(
                    "DownloadJobItemRunner destinationdir is null for ${downloadItem.djiDjUid}")
            UMLog.l(UMLog.CRITICAL, 699,
                    mkLogPrefix() + " destinationDir = null", e)
            throw e
        }

        withContext(coroutineContext) {
            val startDownloadFnJob = launch { startDownload() }
            startDownloadFnJobRef.value = startDownloadFnJob
            UMLog.l(UMLog.INFO, 0, "${mkLogPrefix()} launched download job and got reference")
            startDownloadFnJob.join()
        }
    }


    /**
     * Start downloading a file
     */
    private suspend fun startDownload() = withContext(coroutineContext) {
        UMLog.l(UMLog.INFO, 699,
                "${mkLogPrefix()} StartDownload: ContainerUid = + ${downloadItem.djiContainerUid}")
        var attemptsRemaining = 3

        val container = appDb.containerDao.findByUid(downloadItem.djiContainerUid)

        //Note: the Container must be put in the database by the preparer. Therefor it's better
        // to use the DAO object and avoid any potential to make additional http requests
        val containerManager = ContainerManager(container!!, appDb, appDb, destinationDir!!)

        val currentTimeStamp = getSystemTimeInMillis()
        var downloadStartTime = 0L

        var downloadAttemptStatus = -1
        val destTmpFile = FileSe("$destinationDir/${downloadItem.djiUid}.tmp")
        val containerEntryFileList = mutableListOf<ContainerEntryWithMd5>()
        for (attemptNum in attemptsRemaining downTo 1) {
            //numFailures.value = 0
            currentNetworkNode = localAvailabilityManager.findBestLocalNodeForContentEntryDownload(
                    downloadItem.djiContainerUid)

            val networkNodeToUse = currentNetworkNode
            val isFromCloud = networkNodeToUse == null
            val history = DownloadJobItemHistory()
            history.mode = if (isFromCloud) MODE_CLOUD else MODE_LOCAL
            history.startTime = getSystemTimeInMillis()
            history.downloadJobItemId = downloadItem.djiUid
            history.networkNode = if (isFromCloud) 0L else currentNetworkNode!!.nodeId
            history.id = appDb.downloadJobItemHistoryDao.insert(history).toInt()

            val downloadEndpoint: String?

            if (networkNodeToUse == null) {
                if (connectivityStatus?.wifiSsid != null
                        && connectivityStatus?.wifiSsid?.toUpperCase()?.startsWith("DIRECT-") ?: false) {
                    //we are connected to a local peer, but need the normal wifi
                    //TODO: if the wifi is just not available and is required, don't mark as a failure of this job
                    // set status to waiting for connection and stop
                    launch(mainCoroutineDispatcher) {
                        if (!connectToCloudNetwork()) {
                            //connection has failed
                            attemptsRemaining--
                            recordHistoryFinished(history, false)
                            //continue
                        }
                    }
                }

                if(connectivityStatus?.wifiSsid != null) {
                    networkManager.lockWifi(downloadWiFiLock)
                }

                downloadEndpoint = endpointUrl
            } else {
                if (networkNodeToUse.groupSsid == null
                        || networkNodeToUse.groupSsid != connectivityStatus?.wifiSsid) {
                    if (!connectToLocalNodeNetwork()) {
                        //recording failure will push the node towards the bad threshold, after which
                        // the download will be attempted from the cloud
                        recordHistoryFinished(history, false)
                        continue
                    }
                }

                downloadEndpoint = currentNetworkNode!!.endpointUrl
            }

            val localHttpClient = networkManager.localHttpClient
            if(localHttpClient != null) {
                UMLog.l(UMLog.INFO, 0, "${mkLogPrefix()} using local http client: $localHttpClient")
                currentHttpClient = localHttpClient
            }else {
                UMLog.l(UMLog.INFO, 0, "${mkLogPrefix()} using default http client")
                currentHttpClient = defaultHttpClient()
            }

            history.url = downloadEndpoint

            UMLog.l(UMLog.INFO, 699, mkLogPrefix() +
                    " starting download from " + downloadEndpoint + " FromCloud=" + isFromCloud +
                    " Attempts remaining= " + attemptsRemaining)
            downloadStartTime = getSystemTimeInMillis()

            val containerEntryListVal = currentHttpClient.get<List<ContainerEntryWithMd5>>(
                    "$downloadEndpoint$CONTAINER_ENTRY_LIST_PATH?containerUid=${downloadItem.djiContainerUid}")
            containerEntryFileList.clear()
            containerEntryFileList.addAll(containerEntryListVal)

            entriesDownloaded.value = 0

            val entriesToDownload = containerManager.linkExistingItems(containerEntryListVal).sortedBy { it.cefMd5 }
            existingEntriesBytesDownloaded = containerManager.allEntries
                    .sumByLong { it.containerEntryFile?.ceCompressedSize ?: 0L}

            val entriesListStr = entriesToDownload.joinToString(separator = ";") { it.ceCefUid.toString() }
            val fetchRequest = RequestMpp(UMFileUtil.joinPaths(endpointUrl,
                    ENDPOINT_CONCATENATEDFILES, entriesListStr), destTmpFile.getAbsolutePath())

            history.startTime = getSystemTimeInMillis()
            val currentDownloadAttemptVal  = CompletableDeferred<Int>()
            currentDownloadAttempt.value = currentDownloadAttemptVal

            networkManager.httpFetcher.enqueue(fetchRequest, object: FuncMpp<RequestMpp> {
                override fun call(result: RequestMpp) {
                    currentFetchRequestId = result.id
                }
            },
            object: FuncMpp<Error> {
                override fun call(result: Error) {
                    currentDownloadAttemptVal.completeExceptionally(IOException(result.toString()))
                }
            })


            try {
                downloadAttemptStatus = currentDownloadAttemptVal.await()
                if(downloadAttemptStatus == JobStatus.COMPLETE) {
                    break
                }else {
                    delay(retryDelay)
                }
            }catch(e: Exception) {
                delay(retryDelay)
            }
        }

        if (downloadAttemptStatus == JobStatus.COMPLETE) {
            val downloadTime = getSystemTimeInMillis() - downloadStartTime
            appDb.downloadJobDao.updateBytesDownloadedSoFarAsync(downloadItem.djiDjUid)

            val bytesDownloaded = completedEntriesBytesDownloaded.value
            downloadJobItemManager.updateProgress(downloadItem.djiUid,
                    bytesDownloaded, downloadItem.downloadLength)

            val downloadSpeed = ((bytesDownloaded.toFloat() / 1024f) / (downloadTime.toFloat() / 1000f))
            UMLog.l(UMLog.INFO, 0, "DownloadJob ${downloadItem.djiUid}  Completed " +
                    "download of ${bytesDownloaded}bytes " +
                    "in $downloadTime ms Speed = $downloadSpeed KB/s")

            var concatenatedInputStream: ConcatenatedInputStream? = null
            try {
                concatenatedInputStream = ConcatenatedInputStream(FileInputStreamSe(destTmpFile))
                val pathToMd5Map = containerEntryFileList.map {
                    (it.cePath ?: "") to (it.cefMd5?.base64StringToByteArray() ?: ByteArray(0))
                }.toMap()

                //TODO: handle the situation where one md5 is linked to more than one path
                containerManager.addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = true),
                        pathToMd5Map) {
                    val nextPart = concatenatedInputStream.nextPart()
                    if(nextPart != null) {
                        val partMd5Str = nextPart.id.encodeBase64()
                        val pathsInContainer = containerEntryFileList.filter { it.cefMd5 == partMd5Str }
                        val firstPathInContainer = pathsInContainer.firstOrNull()?.cePath
                        if(firstPathInContainer != null) {
                            ConcatenatedInputStreamEntrySource(nextPart, concatenatedInputStream,
                                    firstPathInContainer)
                        }else {
                            Napier.wtf({"Could not find path for md5sum $partMd5Str"})
                            throw IllegalStateException("Could not find the path of md5sum $partMd5Str")
                        }
                    }else {
                        null
                    }
                }
            }finally {
                concatenatedInputStream?.close()
            }
        }

        stop(downloadAttemptStatus)
    }


    private fun recordHistoryFinished(history: DownloadJobItemHistory, successful: Boolean) {
        history.endTime = getSystemTimeInMillis()
        history.successful = successful
        appDb.downloadJobItemHistoryDao.update(history)
    }

    /**
     * Try to connect to the 'normal' wifi
     *
     * @return true if file should be do downloaded from the cloud otherwise false.
     */
    private suspend fun connectToCloudNetwork(): Boolean {
        UMLog.l(UMLog.DEBUG, 699, "${mkLogPrefix()} Reconnecting cloud network")
        networkManager.restoreWifi()
        waitForLiveData(statusLiveData!!, CONNECTION_TIMEOUT * 1000.toLong()) {
            val checkStatus = connectivityStatus
            when {
                checkStatus == null -> false
                checkStatus.connectivityState == ConnectivityStatus.STATE_UNMETERED -> {
                    true
                }
                else -> checkStatus.connectivityState == STATE_METERED && meteredConnectionAllowed.value == 1
            }
        }

        return connectivityStatus!!.connectivityState == ConnectivityStatus.STATE_UNMETERED
                || (meteredConnectionAllowed.value == 1 && connectivityStatus!!.connectivityState == STATE_METERED)
    }

    /**
     * Start local peers connection handshake
     *
     * Notee: 18/Oct/2019 testing on Android 8 (Dragon 10 tablet) indicates that disconnecting from
     * the current WiFi is not required. The intention is to call the methods as they would be called
     * by the WiFi settings activity. The WiFi settings activity does not call disconnect before
     * calling it's own enableNetwork method.
     *
     * @return true if successful, false otherwise
     */
    private suspend fun connectToLocalNodeNetwork(): Boolean = withContext(coroutineContext) {
        waitingForLocalConnection.value = true
        val requestGroupCreation = BleMessage(WIFI_GROUP_REQUEST,
                BleMessage.getNextMessageIdForReceiver(currentNetworkNode!!.bluetoothMacAddress!!),
                BleMessageUtil.bleMessageLongToBytes(listOf(1L)))
        UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() + " connecting local network: requesting group credentials ")
        connectionRequestActive.value = true
        networkManager.lockWifi(downloadWiFiLock)

        val channel = Channel<Boolean>(1)


        networkManager.sendMessage(context, requestGroupCreation, currentNetworkNode!!, object : BleMessageResponseListener {
            override fun onResponseReceived(sourceDeviceAddress: String, response: BleMessage?, error: Exception?) {
                UMLog.l(UMLog.INFO, 699, mkLogPrefix() +
                        " BLE response received: from " + sourceDeviceAddress + ":" + response +
                        " error: " + error)

                if (connectionRequestActive.value && response != null
                        && response.requestType == WIFI_GROUP_CREATION_RESPONSE) {
                    connectionRequestActive.value = false
                    val lWifiDirectGroup = WiFiDirectGroupBle(response.payload!!)
                    wiFiDirectGroupBle.value = lWifiDirectGroup

                    val acquiredEndPoint = ("http://" + lWifiDirectGroup.ipAddress + ":"
                            + lWifiDirectGroup.port + "/")
                    currentNetworkNode!!.endpointUrl = acquiredEndPoint
                    currentNetworkNode!!.groupSsid = lWifiDirectGroup.ssid
                    UMLog.l(UMLog.INFO, 699, mkLogPrefix() +
                            "Connecting to P2P group network with SSID " + lWifiDirectGroup.ssid)
                    channel.offer(true)
                }


            }

        })

        withTimeoutOrNull(20 * 1000) { channel.receive() }

        connectionRequestActive.value = false


        //There was an exception trying to communicate with the peer to get the wifi direct group network
        if (wiFiDirectGroupBle.value == null) {
            UMLog.l(UMLog.ERROR, 699, mkLogPrefix() +
                    "Requested group network" +
                    "from bluetooth address " + currentNetworkNode!!.bluetoothMacAddress +
                    "but did not receive group network credentials")
            return@withContext false
        }

        UMLog.l(UMLog.INFO, 699, "${mkLogPrefix()}: Initiating connection to " + wiFiDirectGroupBle.value!!.ssid)

        networkManager.connectToWiFi(wiFiDirectGroupBle.value!!.ssid,
                wiFiDirectGroupBle.value!!.passphrase)

        withContext(mainCoroutineDispatcher) {
            waitForLiveData(statusLiveData!!, (lWiFiConnectionTimeout).toLong()) {
                statusRef.value = it
                it != null && isExpectedWifiDirectGroup(it)
            }
        }


        waitingForLocalConnection.value = false
        val currentStatus = statusRef.value
        UMLog.l(UMLog.INFO, 699, "${mkLogPrefix()}: done asking for local connection. " +
                "Status = $currentStatus")
        return@withContext currentStatus != null && isExpectedWifiDirectGroup(currentStatus)
    }


    /**
     * Update status of the currently downloading job item.
     * @param itemStatus new status to be set
     * @see JobStatus
     */
    private suspend fun updateItemStatus(itemStatus: Int) {
        downloadJobItemManager.updateStatus(downloadItem.djiUid, itemStatus)
        UMLog.l(UMLog.INFO, 699,
                "${mkLogPrefix()} Setting status to:  ${JobStatus.statusToString(itemStatus)}")
    }

    private fun isExpectedWifiDirectGroup(status: ConnectivityStatus): Boolean {
        val lWifiDirectGroupBle = wiFiDirectGroupBle.value
        return (status.connectivityState == ConnectivityStatus.STATE_CONNECTED_LOCAL
                && status.wifiSsid != null
                && lWifiDirectGroupBle != null
                && status.wifiSsid == lWifiDirectGroupBle.ssid)
    }


    private fun mkLogPrefix(): String {
        return "DownloadJobItem #" + downloadItem.djiUid + ":"
    }

    companion object {

        internal const val CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5"

        internal const val CONTAINER_ENTRY_FILE_PATH = "ContainerEntryFile/"

        const val BAD_PEER_FAILURE_THRESHOLD = 2

        private const val CONNECTION_TIMEOUT = 60
    }
}
