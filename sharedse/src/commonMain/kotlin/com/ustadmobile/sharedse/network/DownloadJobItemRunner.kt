package com.ustadmobile.sharedse.network

import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.waitForLiveData
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.TAG_MAIN_COROUTINE_CONTEXT
import com.ustadmobile.core.network.containerfetcher.AbstractContainerFetcherListener2
import com.ustadmobile.core.network.containerfetcher.ContainerFetcherRequest2
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadRunner
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.core.util.ext.linkExistingContainerEntries
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.door.ext.doorIdentityHashCode
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_DISCONNECTED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_CLOUD
import com.ustadmobile.lib.db.entities.DownloadJobItemHistory.Companion.MODE_LOCAL
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.sumByLong
import com.ustadmobile.sharedse.io.FileSe
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_CREATION_RESPONSE
import com.ustadmobile.sharedse.network.NetworkManagerBleCommon.Companion.WIFI_GROUP_REQUEST
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import kotlin.jvm.Volatile
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import org.kodein.di.*

data class DownloadJobItemRunnerDIArgs(val endpoint: Endpoint, val downloadJobItem: DownloadJobItem)

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
(private val downloadItem: DownloadJobItem,
 private val endpointUrl: String,
 private val retryDelay: Long = 3000,
 override val di: DI): ContainerDownloadRunner, DIAware {

    private val endpoint = Endpoint(endpointUrl)

    private val containerDownloadManager: ContainerDownloadManager by di.on(endpoint).instance()

    private val appDb: UmAppDatabase by di.on(endpoint).instance(tag = TAG_DB)

    private val networkManager: NetworkManagerBle by di.instance()

    private val mainCoroutineDispatcher: CoroutineDispatcher by di.instance(tag = TAG_MAIN_COROUTINE_CONTEXT)

    private val localAvailabilityManager: LocalAvailabilityManager by di.on(endpoint).instance()

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

    private var downloadManagerHolderRef: Any? = null

    private val timeSinceStart
        get() = getSystemTimeInMillis() - startTime

    private val containerFetcher: ContainerFetcher by instance()


    fun setWiFiConnectionTimeout(lWiFiConnectionTimeout: Int) {
        this.lWiFiConnectionTimeout = lWiFiConnectionTimeout
    }

    override var meteredDataAllowed: Boolean
        get() = meteredConnectionAllowed.value == 1
        set(value) {
            val allowedIntVal = if(value) 1 else 0
            meteredConnectionAllowed.value = allowedIntVal

            val connectivityStatusVal = networkManager.connectivityStatus.getValue()
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
        //this.connectivityStatus = newStatus
        UMLog.l(UMLog.DEBUG, 699, mkLogPrefix() +
                " Connectivity state changed: " + newStatus)
        if (waitingForLocalConnection.value)
            return

        if (newStatus != null) {
            when (newStatus.connectivityState) {
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
                    connectivityStatusObserver?.also {
                        networkManager.connectivityStatus.removeObserver(it)
                    }
                }

                updateItemStatus(newStatus)
                networkManager.releaseWifiLock(downloadWiFiLock)
            }
        }
        Napier.d({"${mkLogPrefix()} stop complete"})
    }


    override suspend fun download(): Deferred<Int> {
        if(downloadCalled) {
            throw IllegalStateException("Can only call download() once on DownloadJobItemRunner!")
        }

        downloadManagerHolderRef = containerDownloadManager.getDownloadJobItemHolderRef(downloadItem.djiUid)
        downloadCalled = true

        startTime = getSystemTimeInMillis()
        println("Download started for  ${downloadItem.djiDjUid}")
        runnerStatus.value = JobStatus.RUNNING
        updateItemStatus(JobStatus.RUNNING)
        val downloadJobId = downloadItem.djiDjUid

        connectivityStatusObserver = ObserverFnWrapper(this::handleConnectivityStatusChanged)

        withContext(mainCoroutineDispatcher) {
            networkManager.connectivityStatus.observeForever(connectivityStatusObserver!!)
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
                ?: throw IllegalArgumentException("startDownload: container ${downloadItem.djiContainerUid} not found!")

        //Note: the Container must be put in the database by the preparer. Therefor it's better
        // to use the DAO object and avoid any potential to make additional http requests
        //val containerManager = ContainerManager(container!!, appDb, appDb, destinationDir!!)

        var downloadStartTime = 0L

        var downloadAttemptStatus = -1
        val destTmpFile = FileSe("$destinationDir/${downloadItem.djiUid}.tmp")

        //list of all container entries in the container - used to
        val containerEntriesList = mutableListOf<ContainerEntryWithMd5>()

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

                val connectivityStatusVal = networkManager.connectivityStatus.getValue()
                val downloadEndpoint: String
                if (networkNodeToUse == null) {
                    if (connectivityStatusVal?.wifiSsid != null
                            && connectivityStatusVal?.wifiSsid?.toUpperCase()?.startsWith("DIRECT-") ?: false) {
                        //we are connected to a local peer, but need the normal wifi
                        //TODO: if the wifi is just not available and is required, don't mark as a failure of this job
                        // set status to waiting for connection and stop

                        //TODO: this looks wrong - why use launch here?
                        withContext(coroutineContext) {
                            launch(mainCoroutineDispatcher) {
                                if (!connectToCloudNetwork()) {
                                    throw Exception("${mkLogPrefix()} could not connect to cloud network")
                                }
                            }
                        }
                    }

                    if(connectivityStatusVal?.wifiSsid != null) {
                        networkManager.lockWifi(downloadWiFiLock)
                    }

                    downloadEndpoint = endpointUrl
                    currentHttpClient = di.direct.instance()
                } else {
                    if (networkNodeToUse.groupSsid == null
                            || connectivityStatusVal?.connectivityState != ConnectivityStatus.STATE_CONNECTED_LOCAL
                            || networkNodeToUse.groupSsid != connectivityStatusVal?.wifiSsid) {
                        if (!connectToLocalNodeNetwork()) {
                            throw Exception("${mkLogPrefix()} could not connect to local node network")
                            //recording failure will push the node towards the bad threshold, after which
                            // the download will be attempted from the cloud
//                            recordHistoryFinished(history, false)
//                            continue
                        }
                    }

                    downloadEndpoint = currentNetworkNode!!.endpointUrl!!
                    currentHttpClient = networkManager.localHttpClient ?: di.direct.instance()
                }

                history.url = downloadEndpoint

                Napier.i(mkLogPrefix() +
                        " starting download from " + downloadEndpoint + " FromCloud=" + isFromCloud +
                        " Attempts remaining= " + attemptsRemaining)
                downloadStartTime = getSystemTimeInMillis()

                val containerEntryListUrl = UMFileUtil.joinPaths(downloadEndpoint,
                        "$CONTAINER_ENTRY_LIST_PATH?containerUid=${downloadItem.djiContainerUid}")
                val containerEntryListVal = currentHttpClient.get<List<ContainerEntryWithMd5>>(
                        containerEntryListUrl)
                containerEntriesList.clear()
                containerEntriesList.addAll(containerEntryListVal)

                entriesDownloaded.value = 0

                val containerEntriesPartition = appDb.linkExistingContainerEntries(container.containerUid,
                    containerEntryListVal)

                existingEntriesBytesDownloaded = containerEntriesPartition.entriesWithMatchingFile
                        .sumByLong { it.containerEntryFile?.ceCompressedSize ?: 0L }


                history.startTime = getSystemTimeInMillis()


                val fetchStartTime = getSystemTimeInMillis()
                Napier.d({"Requesting fetch download $timeSinceStart ms after start"})

                if(containerEntriesPartition.entriesWithoutMatchingFile.isNotEmpty()) {
                    val containerRequest = ContainerFetcherRequest2(
                        containerEntriesPartition.entriesWithoutMatchingFile, siteUrl = endpointUrl,
                        mirrorUrl =  downloadEndpoint,
                        destDirUri = destinationDir ?: throw IllegalStateException("Null destination dir"))
                    var jobDeferred: Deferred<Int>? = null
                    downloadStatusLock.withLock {
                        Napier.d({"${mkLogPrefix()} enqueuing download URL=$downloadEndpoint fileDest=" +
                                destTmpFile.getAbsolutePath()})
                        jobDeferred = containerFetcher.enqueue(containerRequest,
                            object: AbstractContainerFetcherListener2() {
                                override fun onProgress(request: ContainerFetcherRequest2,
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
                }else {
                    downloadAttemptStatus = JobStatus.COMPLETE
                }

                Napier.d({"Fetch over in ${getSystemTimeInMillis() - fetchStartTime}ms status=$downloadAttemptStatus"})
                if(downloadAttemptStatus == JobStatus.COMPLETE) {
                    break
                }
            }catch(e: Exception) {
                Napier.e({"${mkLogPrefix()} exception in download attempt"}, e)
                if(coroutineContext.isActive) {
                    Napier.e({"${mkLogPrefix()} waiting for retry"}, e)
                    delay(retryDelay)
                }
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
        waitForLiveData(networkManager.connectivityStatus, CONNECTION_TIMEOUT * 1000.toLong()) {
            val checkStatus = networkManager.connectivityStatus.getValue()
            when {
                checkStatus == null -> false
                checkStatus.connectivityState == ConnectivityStatus.STATE_UNMETERED -> {
                    true
                }
                else -> checkStatus.connectivityState == STATE_METERED && meteredConnectionAllowed.value == 1
            }
        }

        val connectivityState = networkManager.connectivityStatus.getValue()
        return connectivityState?.connectivityState == ConnectivityStatus.STATE_UNMETERED
                || (meteredConnectionAllowed.value == 1 && connectivityState?.connectivityState == STATE_METERED)
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



        //val channel = Channel<Boolean>(1)


        //TODO: change this to using sendBleMessage instead
        val nodeBleAddr = currentNetworkNode?.bluetoothMacAddress ?: return@withContext false
        val response = networkManager.sendBleMessage(requestGroupCreation, nodeBleAddr)
        val responsePayload = response?.payload
        if(response?.requestType == WIFI_GROUP_CREATION_RESPONSE && responsePayload != null) {
            val lWifiDirectGroup = WiFiDirectGroupBle(responsePayload).also {
                wiFiDirectGroupBle.value = it
            }

            val acquiredEndPoint = ("http://" + lWifiDirectGroup.ipAddress + ":"
                    + lWifiDirectGroup.port + "/")
            currentNetworkNode?.apply {
                endpointUrl = UMFileUtil.joinPaths(acquiredEndPoint,
                        UMURLEncoder.encodeUTF8(endpoint.url)) + "/"
                groupSsid = lWifiDirectGroup.ssid
            }
        }
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
            waitForLiveData(networkManager.connectivityStatus, (lWiFiConnectionTimeout).toLong()) {
                statusRef.value = it
                isExpectedWifiDirectGroup(it)
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
        return "DownloadJobItem #" + downloadItem.djiUid + " (${this.doorIdentityHashCode}) :"
    }

    companion object {

        internal const val CONTAINER_ENTRY_LIST_PATH = "ContainerEntryList/findByContainerWithMd5"

        internal const val CONTAINER_ENTRY_FILE_PATH = "ContainerEntryFile/"

        const val BAD_PEER_FAILURE_THRESHOLD = 2

        private const val CONNECTION_TIMEOUT = 60

    }
}
