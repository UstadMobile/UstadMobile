package com.ustadmobile.sharedse.network

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_DISCONNECTED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_NONE
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_CLOUD
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_LOCAL
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.sharedse.io.FileInputStreamSe
import com.ustadmobile.sharedse.io.FileSe
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.atomicfu.AtomicLongArray
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.io.IOException
import kotlin.coroutines.coroutineContext

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
 private val numConcurrentEntryDownloads: Int = 4) {

    //TODO: This should be converted to use openDownloadJobItemManager
    private val downloadJobItemManager: DownloadJobItemManager = networkManager.getDownloadJobItemManager(downloadItem.djiDjUid)!!

    private var statusLiveData: DoorLiveData<ConnectivityStatus?>? = null

    private var statusObserver: DoorObserver<ConnectivityStatus?>? = null

    //TODO: enable switching to local download when available after basic p2p cases complete
    //private UmObserver<EntryStatusResponse> entryStatusObserver;

    //private UmLiveData<EntryStatusResponse> entryStatusLiveData;

    private var downloadJobItemObserver: DoorObserver<Int>? = null

    private var downloadJobItemLiveData: DoorLiveData<Int>? = null

    private var downloadSetConnectivityData: DoorLiveData<Boolean>? = null

    private var downloadSetConnectivityObserver: DoorObserver<Boolean>? = null

    private val completedEntriesBytesDownloaded = atomic(0L)

    private val runnerStatus = atomic(JobStatus.NOT_QUEUED)

    private val meteredConnectionAllowed = atomic(-1)

    private var lWiFiConnectionTimeout = 30

    private val wiFiDirectGroupBle = atomic<WiFiDirectGroupBle?>(null)

    private var currentNetworkNode: NetworkNode? = null

    private lateinit var currentHttpClient: HttpClient

    /**
     * Boolean to indicate if we are waiting for a local connection.
     */
    private val waitingForLocalConnection = atomic(false)

    private val downloadWiFiLock = Any()

    private var destinationDir: String? = null

    private val numFailures = atomic(0)

    private val entriesDownloaded = atomic(0)

    private val connectionRequestActive = atomic(false)

    private val statusRef = atomic<ConnectivityStatus?>(null)

    var startDownloadFnJob: Job? = null

    private val inProgressDownloadCounters = AtomicLongArray(numConcurrentEntryDownloads)

    class DownloadedEntrySource(override val pathInContainer: String,
                                private val file: FileSe,
                                override val md5Sum: ByteArray,
                                override val filePath: String,
                                override val compression: Int = 0) : ContainerManagerCommon.EntrySource {
        override val length: Long
            get() = file.length()

        override val inputStream = FileInputStreamSe(file)

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

//TODO: re-enable when we add support for switching dynamically
//    /**
//     * Handle changes triggered when file which wasn't available locally changes
//     * @param entryStatusResponse new file entry status
//     */
//    private void handleContentEntryFileStatus(EntryStatusResponse entryStatusResponse){
//        if(entryStatusResponse != null){
//            availableLocally.set(entryStatusResponse.isAvailable() ? 1:0);
//            if(availableLocally.get() == 1 && currentEntryStatusResponse!= null
//                    && !currentEntryStatusResponse.isAvailable()){
//                this.currentNetworkNode =
//                        appDb.getNetworkNodeDao().findNodeById(entryStatusResponse.getErNodeId());
//                connectToLocalNodeNetwork();
//            }
//        }
//    }


    /**
     * Stop the download task from continuing (if not already stopped). Calling stop for a second
     * time will have no effect.
     *
     * @param newStatus new status to be set
     */
    suspend fun stop(newStatus: Int, cancel: Boolean = false) {
        if (!runnerStatus.compareAndSet(JobStatus.STOPPED, JobStatus.STOPPED)) {
            if (cancel) {
                println("===CANCELLING $startDownloadFnJob===")
                startDownloadFnJob?.cancel()
            }

            withContext(mainCoroutineDispatcher) {
                statusLiveData!!.removeObserver(statusObserver!!)
                downloadJobItemLiveData!!.removeObserver(downloadJobItemObserver!!)
                downloadSetConnectivityData!!.removeObserver(downloadSetConnectivityObserver!!)
            }

            updateItemStatus(newStatus)
            networkManager.releaseWifiLock(this)
        }

    }


    suspend fun download() {
        println("Download started for  ${downloadItem.djiDjUid}")
        runnerStatus.value = JobStatus.RUNNING
        updateItemStatus(JobStatus.RUNNING)
        val downloadJobId = downloadItem.djiDjUid
        appDb.downloadJobDao.update(downloadJobId, JobStatus.RUNNING)

        networkManager.startMonitoringAvailability(this,
                listOf(downloadItem.djiContainerUid))

        statusLiveData = appDb.connectivityStatusDao.statusLive()
        downloadJobItemLiveData = appDb.downloadJobItemDao.getLiveStatus(downloadItem.djiUid)

        //get the download set
        downloadSetConnectivityData = appDb.downloadJobDao.getLiveMeteredNetworkAllowed(downloadJobId)

        //TODO: re-enable after basic p2p cases run
        //        entryStatusLiveData = appDb.getEntryStatusResponseDao()
        //                .getLiveEntryStatus(downloadItem.getDjiContentEntryFileUid());

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
        //entryStatusObserver = this::handleContentEntryFileStatus;

        //entryStatusLiveData.observeForever(entryStatusObserver);

        destinationDir = appDb.downloadJobDao.getDestinationDir(downloadJobId)
        if (destinationDir == null) {
            val e = IllegalArgumentException(
                    "DownloadJobItemRunner destinationdir is null for " + downloadItem.djiDjUid)
            UMLog.l(UMLog.CRITICAL, 699,
                    mkLogPrefix() + " destinationDir = null", e)
            throw e
        }

        withContext(coroutineContext) {
            startDownloadFnJob = launch { startDownload() }
            startDownloadFnJob!!.join()
        }
    }


    /**
     * Start downloading a file
     */
    private suspend fun startDownload() = withContext(coroutineContext) {
        UMLog.l(UMLog.INFO, 699,
                "${mkLogPrefix()} StartDownload: ContainerUid = + ${downloadItem.djiContainerUid}")
        var attemptsRemaining = 3

        val container = appDbRepo.containerDao
                .findByUid(downloadItem.djiContainerUid)

        val containerManager = ContainerManager(container!!, appDb, appDbRepo, destinationDir!!)

        val currentTimeStamp = getSystemTimeInMillis()
        val minLastSeen = currentTimeStamp - (60 * 1000)
        val maxFailureFromTimeStamp = currentTimeStamp - (5 * 60 * 1000)

        var numEntriesToDownload = -1

        val progressUpdater = async {
            while (isActive) {
                delay(1000)
                var totalInProgress = 0L
                for (i in 0 until numConcurrentEntryDownloads) {
                    totalInProgress += inProgressDownloadCounters[i].value
                }
                val downloadSoFar = totalInProgress + completedEntriesBytesDownloaded.value
                downloadJobItemManager.updateProgress(downloadItem.djiUid, downloadSoFar, downloadItem.downloadLength)
            }
        }

        for (attemptNum in attemptsRemaining downTo 1) {
            numEntriesToDownload = -1
            numFailures.value = 0
            //TODO: if the content is available on the node we already connected to, take that one
            currentNetworkNode = appDb.networkNodeDao
                    .findLocalActiveNodeByContainerUid(downloadItem.djiContainerUid,
                            minLastSeen, BAD_PEER_FAILURE_THRESHOLD, maxFailureFromTimeStamp)

            val isFromCloud = currentNetworkNode == null
            val history = DownloadJobItemHistory()
            history.mode = if (isFromCloud) MODE_CLOUD else MODE_LOCAL
            history.startTime = getSystemTimeInMillis()
            history.downloadJobItemId = downloadItem.djiUid
            history.networkNode = if (isFromCloud) 0L else currentNetworkNode!!.nodeId
            history.id = appDb.downloadJobItemHistoryDao.insert(history).toInt()

            val downloadEndpoint: String?

            if (isFromCloud) {
                if (connectivityStatus!!.wifiSsid != null && connectivityStatus!!.wifiSsid!!.toUpperCase().startsWith("DIRECT-")) {
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
                downloadEndpoint = endpointUrl
            } else {
                if (currentNetworkNode!!.groupSsid == null || currentNetworkNode!!.groupSsid != connectivityStatus!!.wifiSsid) {

                    if (!connectToLocalNodeNetwork()) {
                        //recording failure will push the node towards the bad threshold, after which
                        // the download will be attempted from the cloud
                        recordHistoryFinished(history, false)
                        //continue
                    }
                }

                downloadEndpoint = currentNetworkNode!!.endpointUrl
            }

            currentHttpClient = (if (networkManager.localHttpClient != null) networkManager.localHttpClient else defaultHttpClient())!!


            history.url = downloadEndpoint

            UMLog.l(UMLog.INFO, 699, mkLogPrefix() +
                    " starting download from " + downloadEndpoint + " FromCloud=" + isFromCloud +
                    " Attempts remaining= " + attemptsRemaining)

            try {
                appDb.downloadJobItemDao.incrementNumAttempts(downloadItem.djiUid)

                val containerEntryList = currentHttpClient.get<List<ContainerEntryWithMd5>>(
                        "$downloadEndpoint$CONTAINER_ENTRY_LIST_PATH?containerUid=${downloadItem.djiContainerUid}")
                entriesDownloaded.value = 0

                val entriesToDownload = containerManager.linkExistingItems(containerEntryList)
                numEntriesToDownload = entriesToDownload.size
                history.startTime = getSystemTimeInMillis()

                withContext(coroutineContext) {
                    val producer = produce {
                        entriesToDownload.forEach { send(it) }
                    }

                    repeat(numConcurrentEntryDownloads) { procNum ->
                        launch {
                            for (entry in producer) {
                                val destFile = FileSe(FileSe(destinationDir!!),
                                        entry.ceCefUid.toString() + ".tmp")
                                val downloadUrl = downloadEndpoint + CONTAINER_ENTRY_FILE_PATH + entry.ceCefUid
                                UMLog.l(UMLog.VERBOSE, 100, "Downloader $procNum $downloadUrl -> $destFile")
                                val resumableDownload = ResumableDownload2(downloadUrl,
                                        destFile.getAbsolutePath(), httpClient = currentHttpClient)
                                resumableDownload.onDownloadProgress = { inProgressDownloadCounters[procNum].value = it }
                                if (resumableDownload.download()) {
                                    entriesDownloaded.incrementAndGet()
                                    inProgressDownloadCounters[procNum].value = 0L
                                    val completedDl = destFile.length()
                                    completedEntriesBytesDownloaded.addAndGet(completedDl)


                                    var `is`: FileInputStreamSe? = null
                                    var compression = COMPRESSION_NONE
                                    try {
                                        val sign = ByteArray(2)
                                        `is` = FileInputStreamSe(destFile)
                                        val magic = `is`.read(sign)
                                        compression = if (magic == 2 && sign[0] == 0x1f.toByte() && sign[1] == 0x8b.toByte()) {
                                            COMPRESSION_GZIP
                                        } else {
                                            COMPRESSION_NONE
                                        }
                                    } catch (io: IOException) {

                                    } finally {
                                        `is`?.close()
                                    }

                                    containerManager.addEntries(
                                            ContainerManagerCommon.AddEntryOptions(moveExistingFiles = true,
                                                    dontUpdateTotals = true),
                                            DownloadedEntrySource(entry.cePath!!, destFile,
                                                    resumableDownload.md5Sum, destFile.getAbsolutePath(), compression))
                                } else {
                                    numFailures.incrementAndGet()
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                UMLog.l(UMLog.ERROR, 699, mkLogPrefix() +
                        "Failed to download a file from " + endpointUrl, e)
            }


            //delay(10000)

            val numFails = numFailures.value
            recordHistoryFinished(history, numFails == 0)
            if (numEntriesToDownload != -1 && entriesDownloaded.value == numEntriesToDownload) {
                break
            } else {
                //wait before retry
                delay(retryDelay)
            }
        }


        val downloadCompleted = numEntriesToDownload != -1 && entriesDownloaded.value == numEntriesToDownload
        if (downloadCompleted) {
            appDb.downloadJobDao.updateBytesDownloadedSoFarAsync(downloadItem.djiDjUid)

            downloadJobItemManager.updateProgress(downloadItem.djiUid,
                    completedEntriesBytesDownloaded.value, downloadItem.downloadLength)
        }

        progressUpdater.cancel()
        stop(if (downloadCompleted) JobStatus.COMPLETE else JobStatus.FAILED)
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
        UMLog.l(UMLog.DEBUG, 699, "Reconnecting cloud network")
        networkManager.restoreWifi()
        waitForLiveData(statusLiveData!!, CONNECTION_TIMEOUT * 1000.toLong()) {
            val checkStatus = connectivityStatus
            when {
                checkStatus == null -> false
                checkStatus.connectivityState == ConnectivityStatus.STATE_UNMETERED -> {
                    networkManager.lockWifi(downloadWiFiLock)
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
                    appDb.networkNodeDao.updateNetworkNodeGroupSsid(currentNetworkNode!!.nodeId,
                            lWifiDirectGroup.ssid, acquiredEndPoint)

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

        //disconnect first
        if (connectivityStatus!!.connectivityState != STATE_DISCONNECTED && connectivityStatus!!.wifiSsid != null) {
            launch(mainCoroutineDispatcher) {
                waitForLiveData(statusLiveData!!, 10 * 1000.toLong()) {
                    it != null && it.connectivityState != ConnectivityStatus.STATE_UNMETERED
                }
                UMLog.l(UMLog.INFO, 699, "Disconnected existing wifi network")
            }
        }

        UMLog.l(UMLog.INFO, 699, "Connection initiated to " + wiFiDirectGroupBle.value!!.ssid)

        networkManager.connectToWiFi(wiFiDirectGroupBle.value!!.ssid,
                wiFiDirectGroupBle.value!!.passphrase)


        launch(mainCoroutineDispatcher) {
            waitForLiveData(statusLiveData!!, (lWiFiConnectionTimeout * 1000).toLong()) {
                statusRef.value = it
                it != null && isExpectedWifiDirectGroup(it)
            }
        }

        waitingForLocalConnection.value = false
        val currentStatus = statusRef.value
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
