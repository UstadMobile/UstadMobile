package com.ustadmobile.sharedse.network

import com.github.aakira.napier.Napier
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
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
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
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.sharedse.network.containerfetcher.AbstractContainerFetcherListener
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherRequest
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
 * @param containerDownloadManager The containerdownloadmanager that is controlling this download
 * @param networkManager BLE network manager for network operation controls.
 * @param appDb Application database instance
 * @param endpointUrl Endpoint to get the file from.
 * @param mainCoroutineDispatcher A coroutine dispatcher that will, on Android, dispatch on the main
 * thread. This is required because Room's LiveData.observeForever must be called from the main thread
 */
(private val context: Any,
 private val downloadItem: DownloadJobItem,
 private val containerDownloadManager: ContainerDownloadManager,
 private val networkManager: NetworkManagerBleCommon,
 private val appDb: UmAppDatabase,
 private val endpointUrl: String,
 private var connectivityStatus: ConnectivityStatus?,
 private val connectivityStatusLiveData: DoorLiveData<ConnectivityStatus?>,
 private val retryDelay: Long = 3000,
 private val mainCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
 private val localAvailabilityManager: LocalAvailabilityManager): ContainerDownloadRunner {

    private var connectivityStatusObserver: DoorObserver<ConnectivityStatus?>? = null

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

    private val entriesDownloaded = atomic(0)

    private val connectionRequestActive = atomic(false)

    private val statusRef = atomic<ConnectivityStatus?>(null)

    val startDownloadFnJobRef = atomic<Job?>(null)

    private val currentDownloadAttempt = atomic(null as Deferred<Int>?)

    private var existingEntriesBytesDownloaded = 0L

    /**
     * Lock used for any operation that is changing the status of the Fetch download
     */
    private val downloadStatusLock = Mutex()

    private var startTime = 0L

    @Volatile
    private var downloadCalled: Boolean = false

    private var startDownloadFnJob: Deferred<Int>? = null

    private val timeSinceStart
        get() = getSystemTimeInMillis() - startTime

    fun setWiFiConnectionTimeout(lWiFiConnectionTimeout: Int) {
        this.lWiFiConnectionTimeout = lWiFiConnectionTimeout
    }

    override var meteredDataAllowed: Boolean
        get() = meteredConnectionAllowed.value == 1
        set(value) {
            val allowedIntVal = if(value) 1 else 0
            meteredConnectionAllowed.value = allowedIntVal

            val connectivityStatusVal = connectivityStatus
            val meteredConnectionAllowedVal = meteredConnectionAllowed.value
            if (meteredConnectionAllowedVal == 0 && connectivityStatusVal != null
                    && connectivityStatusVal.connectivityState == STATE_METERED) {
                UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() + " : no longer allowed to run on metered network - stopping")
                GlobalScope.launch { stop(JobStatus.QUEUED, cancel = true) }
            }
        }

    /**
     * Handle changes triggered when connectivity status changes.
     * @param newStatus changed connectivity status
     */
    internal fun handleConnectivityStatusChanged(newStatus: ConnectivityStatus?) {
        this.connectivityStatus = newStatus
        UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() +
                " Connectivity state changed: " + newStatus)
        if (waitingForLocalConnection.value)
            return

        if (connectivityStatus != null) {
            when (newStatus!!.connectivityState) {
                STATE_METERED -> if (meteredConnectionAllowed.value == 0) {
                    Napier.d({"${mkLogPrefix()} Connectivity state changed: on metered connection, " +
                            "must stop now."})
                    GlobalScope.launch { stop(JobStatus.QUEUED, cancel = true) }
                }

                STATE_DISCONNECTED -> GlobalScope.launch { stop(JobStatus.QUEUED, cancel = true) }
            }
        }
    }

    override suspend fun cancel() {
        GlobalScope.launch { stop(JobStatus.CANCELED, cancel = true) }
    }

    override suspend fun pause() {
        GlobalScope.launch { stop(JobStatus.PAUSED, cancel = true) }
    }

    /**
     * Stop the download task from continuing (if not already stopped). Calling stop for a second
     * time will have no effect.
     *
     * @param newStatus new status to be set
     */
    suspend fun stop(newStatus: Int, cancel: Boolean = false) {
        Napier.d({"${mkLogPrefix()} stopping - waiting for lock"})
        downloadStatusLock.withLock {
            if (runnerStatus.getAndSet(JobStatus.STOPPED) != JobStatus.STOPPED) {
                Napier.d({"${mkLogPrefix()} stopping - checking cancel"})
                if (cancel) {
                    startDownloadFnJob?.cancelAndJoin()
                    currentDownloadAttempt.value?.cancelAndJoin()
                }

                withContext(mainCoroutineDispatcher) {
                    connectivityStatusLiveData.removeObserver(connectivityStatusObserver!!)
                }

                updateItemStatus(newStatus)
                networkManager.releaseWifiLock(downloadWiFiLock)
            }
        }
    }


    suspend override fun download(): Deferred<Int> {
        if(downloadCalled) {
            throw IllegalStateException("Can only call download() once on DownloadJobItemRunner!")
        }
        downloadCalled = true

        startTime = getSystemTimeInMillis()
        println("Download started for  ${downloadItem.djiDjUid}")
        runnerStatus.value = JobStatus.RUNNING
        updateItemStatus(JobStatus.RUNNING)
        val downloadJobId = downloadItem.djiDjUid

        connectivityStatusObserver = ObserverFnWrapper(this::handleConnectivityStatusChanged)

        withContext(mainCoroutineDispatcher) {
            connectivityStatusLiveData.observeForever(connectivityStatusObserver!!)
        }

        destinationDir = appDb.downloadJobDao.getDestinationDir(downloadJobId)
        val meteredConnectionAllowedVal = if(appDb.downloadJobDao.getMeteredNetworkAllowed(downloadJobId)) {
            1
        }else {
            0
        }
        meteredConnectionAllowed.value = meteredConnectionAllowedVal

        if (destinationDir == null) {
            val e = IllegalArgumentException(
                    "DownloadJobItemRunner destinationdir is null for ${downloadItem.djiDjUid}")
            UMLog.l(UMLog.CRITICAL, 699,
                    mkLogPrefix() + " destinationDir = null", e)
            throw e
        }

        return GlobalScope.async { startDownload() }.also { startDownloadFnJob = it }
    }


    /**
     * Start downloading a file
     */
    private suspend fun startDownload(): Int {
        UMLog.l(UMLog.INFO, 699,
                "${mkLogPrefix()} StartDownload: ContainerUid = + ${downloadItem.djiContainerUid}")
        val attemptsRemaining = 3

        val container = appDb.containerDao.findByUid(downloadItem.djiContainerUid)

        //Note: the Container must be put in the database by the preparer. Therefor it's better
        // to use the DAO object and avoid any potential to make additional http requests
        val containerManager = ContainerManager(container!!, appDb, appDb, destinationDir!!)

        var downloadStartTime = 0L

        var downloadAttemptStatus = -1
        val destTmpFile = FileSe("$destinationDir/${downloadItem.djiUid}.tmp")

        //list of all container entries in the container - used to
        val containerEntriesList = mutableListOf<ContainerEntryWithMd5>()

        val containerEntriesToDownloadList = mutableListOf<ContainerEntryWithMd5>()

        var attemptNum = 0
        while(attemptNum++ < 3 && coroutineContext.isActive) {
            try {
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

                val downloadEndpoint: String
                if (networkNodeToUse == null) {
                    if (connectivityStatus?.wifiSsid != null
                            && connectivityStatus?.wifiSsid?.toUpperCase()?.startsWith("DIRECT-") ?: false) {
                        //we are connected to a local peer, but need the normal wifi
                        //TODO: if the wifi is just not available and is required, don't mark as a failure of this job
                        // set status to waiting for connection and stop
                        withContext(coroutineContext) {
                            launch(mainCoroutineDispatcher) {
                                if (!connectToCloudNetwork()) {
                                    throw IOException("${mkLogPrefix()} could not connect to cloud network")
                                }
                            }
                        }
                    }

                    if(connectivityStatus?.wifiSsid != null) {
                        networkManager.lockWifi(downloadWiFiLock)
                    }

                    downloadEndpoint = endpointUrl
                    currentHttpClient = defaultHttpClient()
                } else {
                    if (networkNodeToUse.groupSsid == null
                            || connectivityStatus?.connectivityState != ConnectivityStatus.STATE_CONNECTED_LOCAL
                            || networkNodeToUse.groupSsid != connectivityStatus?.wifiSsid) {
                        if (!connectToLocalNodeNetwork()) {
                            throw IOException("${mkLogPrefix()} could not connect to local node network")
                            //recording failure will push the node towards the bad threshold, after which
                            // the download will be attempted from the cloud
//                            recordHistoryFinished(history, false)
//                            continue
                        }
                    }

                    downloadEndpoint = currentNetworkNode!!.endpointUrl!!
                    currentHttpClient = networkManager.localHttpClient ?: defaultHttpClient()
                }

                history.url = downloadEndpoint

                UMLog.l(UMLog.INFO, 699, mkLogPrefix() +
                        " starting download from " + downloadEndpoint + " FromCloud=" + isFromCloud +
                        " Attempts remaining= " + attemptsRemaining)
                downloadStartTime = getSystemTimeInMillis()

                val containerEntryListVal = currentHttpClient.get<List<ContainerEntryWithMd5>>(
                        "$downloadEndpoint$CONTAINER_ENTRY_LIST_PATH?containerUid=${downloadItem.djiContainerUid}")
                containerEntriesList.clear()
                containerEntriesList.addAll(containerEntryListVal)

                entriesDownloaded.value = 0

                val entriesToDownloadVal = containerManager.linkExistingItems(containerEntryListVal)
                        .distinctBy { it.cefMd5 }
                        .sortedBy { it.cefMd5 }
                containerEntriesToDownloadList.clear()
                containerEntriesToDownloadList.addAll(entriesToDownloadVal)

                existingEntriesBytesDownloaded = containerManager.allEntries
                        .sumByLong { it.containerEntryFile?.ceCompressedSize ?: 0L}

                val entriesListStr = entriesToDownloadVal.joinToString(separator = ";") { it.ceCefUid.toString() }


                history.startTime = getSystemTimeInMillis()


                val fetchStartTime = getSystemTimeInMillis()
                Napier.d({"Requesting fetch download $timeSinceStart ms after start"})
                val downloadUrl = UMFileUtil.joinPaths(downloadEndpoint, ENDPOINT_CONCATENATEDFILES,
                        entriesListStr)
                val containerRequest = ContainerFetcherRequest(downloadUrl,
                        destTmpFile.getAbsolutePath())
                var jobDeferred: Deferred<Int>? = null
                downloadStatusLock.withLock {
                    Napier.d({"${mkLogPrefix()} enqueuing download URL=$downloadUrl fileDest=" +
                            destTmpFile.getAbsolutePath()})
                    jobDeferred = networkManager.containerFetcher.enqueue(containerRequest,
                            object: AbstractContainerFetcherListener() {
                                override fun onProgress(request: ContainerFetcherRequest,
                                                        bytesDownloaded: Long, contentLength: Long) {
                                    GlobalScope.launch {
                                        downloadItem.downloadedSoFar = existingEntriesBytesDownloaded + bytesDownloaded
                                        containerDownloadManager.handleDownloadJobItemUpdated(
                                                DownloadJobItem(downloadItem))
                                    }
                                }
                            })
                    Napier.d({"${mkLogPrefix()} download queued"})
                    currentDownloadAttempt.value = jobDeferred
                }
                downloadAttemptStatus = jobDeferred?.await() ?: JobStatus.FAILED

                Napier.d({"Fetch over in ${getSystemTimeInMillis() - fetchStartTime}ms status=$downloadAttemptStatus"})
                if(downloadAttemptStatus == JobStatus.COMPLETE) {
                    break
                }
            }catch(e: Exception) {
                Napier.e({"${mkLogPrefix()} exception in download attempt"}, e)
                if(coroutineContext.isActive)
                    delay(retryDelay)
            }finally {

            }
        }

        if (downloadAttemptStatus == JobStatus.COMPLETE) {
            val downloadTime = getSystemTimeInMillis() - downloadStartTime
            downloadItem.downloadedSoFar = downloadItem.downloadLength
            containerDownloadManager.handleDownloadJobItemUpdated(DownloadJobItem(downloadItem))

            val downloadSpeed = ((downloadItem.downloadedSoFar.toFloat() / 1024f) / (downloadTime.toFloat() / 1000f))
            UMLog.l(UMLog.INFO, 0, "DownloadJob ${downloadItem.djiUid}  Completed " +
                    "download of ${downloadItem.downloadedSoFar}bytes " +
                    "in $downloadTime ms Speed = $downloadSpeed KB/s")

            var concatenatedInputStream: ConcatenatedInputStream? = null

            //TODO Here: Make the download fail if the validation does not check out, don't crash the app
            try {
                concatenatedInputStream = ConcatenatedInputStream(FileInputStreamSe(destTmpFile))
                val pathToMd5Map = containerEntriesToDownloadList.map {
                    (it.cePath ?: "") to (it.cefMd5?.base64StringToByteArray() ?: ByteArray(0))
                }.toMap()

                var entryCount = 0
                containerManager.addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = true),
                        pathToMd5Map) {

                    val nextPart = concatenatedInputStream.nextPart()
                    if(nextPart != null && !(startDownloadFnJobRef.value?.isCancelled ?: false)) {
                        val partMd5Str = nextPart.id.encodeBase64()
                        val containerEntry = containerEntriesToDownloadList[entryCount]
                        if(containerEntry.cefMd5 != partMd5Str) {
                            Napier.wtf({"Wrong MD5 Sum in response! $partMd5Str"})
                            throw IllegalStateException("Could not find the path of md5sum $partMd5Str")
                        }

                        val pathsInContainer = containerEntriesList.filter {
                            it.cefMd5 == partMd5Str && it.cePath != null
                        }
                        if(pathsInContainer.isNotEmpty()) {
                            entryCount++
                            ConcatenatedInputStreamEntrySource(nextPart, concatenatedInputStream,
                                    pathsInContainer.map { it.cePath!! })
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

        stop(if(downloadAttemptStatus != -1) downloadAttemptStatus else JobStatus.FAILED)

        return downloadAttemptStatus
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
        waitForLiveData(connectivityStatusLiveData!!, CONNECTION_TIMEOUT * 1000.toLong()) {
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
            waitForLiveData(connectivityStatusLiveData, (lWiFiConnectionTimeout).toLong()) {
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
        downloadItem.djiStatus = itemStatus
        containerDownloadManager.handleDownloadJobItemUpdated(DownloadJobItem(downloadItem))

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
