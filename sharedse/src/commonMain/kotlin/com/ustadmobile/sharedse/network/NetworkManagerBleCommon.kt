package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.sharedMutableMapOf
import com.ustadmobile.sharedse.util.EntryTaskExecutor
import com.ustadmobile.sharedse.util.LiveDataWorkQueue
import io.ktor.client.HttpClient
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlin.collections.set
import kotlin.jvm.Synchronized

/**
 * This is an abstract class which is used to implement platform specific NetworkManager
 *
 *
 * @property context system context to use
 * @property singleThreadDispatcher A single thread based dispatcher that is used for tracking download
 *                                  status. DownloadJobItemManager requires a single thread environment
 *
 * @author kileha3
 */
abstract class NetworkManagerBleCommon(
        val context: Any = Any(),
        private val singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default,
        private val mainDispatcher: CoroutineDispatcher = Dispatchers.Default,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
        internal var umAppDatabase: UmAppDatabase = UmAppDatabase.getInstance(context),
        internal var umAppDatabaseRepo: UmAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)) :
        DownloadJobItemStatusProvider {

    private val knownNodesLock = Any()

    private var isStopMonitoring = false


    private val availabilityMonitoringRequests = mutableMapOf<Any, List<Long>>()

    /**
     * Android 5+ requires using a bound socketFactory from the network object to route traffic over
     * a WiFi network that has no Internet.
     *
     * The underlying implementation will create this HttpClient object when connecting to a network
     * with no Internet access.
     */
    var localHttpClient: HttpClient? = null
        protected set

    /**
     * Holds all created entry status tasks
     */
    private val entryStatusTasks = mutableListOf<BleEntryStatusTask>()

    private lateinit var downloadJobItemWorkQueue: LiveDataWorkQueue<DownloadJobItem>

    private val entryStatusResponses = mutableMapOf<Long, MutableList<EntryStatusResponse>>()

    private val locallyAvailableContainerUids = mutableSetOf<Long>()

    protected val connectivityStatusRef = atomic(null as ConnectivityStatus?)

    protected var wifiLockHolders = mutableListOf<Any>()

    private val knownPeerNodes = mutableMapOf<String, Long>()

    private var jobItemManagerList: DownloadJobItemManagerList? = null

    private val entryStatusTaskExecutor = EntryTaskExecutor(5)

    private lateinit var nextDownloadItemsLiveData: DoorLiveData<List<DownloadJobItem>>

    internal class DownloadQueueLocalAvailabilityObserver(val localAvailabilityManager: LocalAvailabilityManager): DoorObserver<List<DownloadJobItem>> {

        internal var currentRequest: AvailabilityMonitorRequest? = null

        override fun onChanged(t: List<DownloadJobItem>) {
            val prevRequest = currentRequest
            if(prevRequest != null)
                localAvailabilityManager.removeMonitoringRequest(prevRequest)

            val newRequest = if(t.isNotEmpty()){
                AvailabilityMonitorRequest(t.map { it.djiContainerUid }, {})
            }else {
                null
            }

            currentRequest = newRequest
            if(newRequest != null) {
                localAvailabilityManager.addMonitoringRequest(newRequest)
            }
        }
    }


    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean true, if enabled otherwise false.
     */
    abstract val isWiFiEnabled: Boolean


    /**
     * Check if the device is Bluetooth Low Energy capable
     * @return True is capable otherwise false
     */
    abstract val isBleCapable: Boolean

    /**
     * Check if bluetooth is enabled on the device
     * @return True if enabled otherwise false
     */
    abstract val isBluetoothEnabled: Boolean

    /**
     * Get all unique entry UUID's to be monitored
     * @return Set of all unique UUID's
     */
    private val allUidsToBeMonitored: Set<Long>
        get() = availabilityMonitoringRequests.flatMap { it.value }.toSet()


    abstract val isVersionLollipopOrAbove: Boolean

    abstract val isVersionKitKatOrBelow: Boolean

    val activeDownloadJobItemManagers
        get() = jobItemManagerList!!.activeDownloadJobItemManagers


    val localAvailabilityManager: LocalAvailabilityManagerImpl = LocalAvailabilityManagerImpl(context,
            this::makeEntryStatusTask, singleThreadDispatcher)

    private val downloadQueueLocalAvailabilityObserver = DownloadQueueLocalAvailabilityObserver(localAvailabilityManager)

    /**
     * Only for testing - allows the unit test to set this without running the main onCreate method
     *
     * @param jobItemManagerList DownloadJobItemManagerList
     */
    fun setJobItemManagerList(jobItemManagerList: DownloadJobItemManagerList) {
        this.jobItemManagerList = jobItemManagerList
    }

    /**
     * Start web server, advertising and discovery
     */
    open fun onCreate() {
        jobItemManagerList = DownloadJobItemManagerList(umAppDatabase, singleThreadDispatcher)
        nextDownloadItemsLiveData = umAppDatabase.downloadJobItemDao.findNextDownloadJobItems()
        downloadJobItemWorkQueue = LiveDataWorkQueue(nextDownloadItemsLiveData,
                { item1, item2 -> item1.djiUid == item2.djiUid },
                mainDispatcher = mainDispatcher,
                onItemStarted = this::onDownloadJobItemStarted,
                onQueueEmpty = this::onDownloadQueueEmpty) {
            DownloadJobItemRunner(context, it, this@NetworkManagerBleCommon,
                    umAppDatabase, umAppDatabaseRepo, UmAccountManager.getActiveEndpoint(context)!!,
                    connectivityStatusRef.value, mainCoroutineDispatcher = mainDispatcher,
                    ioCoroutineDispatcher = ioDispatcher,
                    localAvailabilityManager = localAvailabilityManager).download()
        }
        nextDownloadItemsLiveData.observeForever(downloadQueueLocalAvailabilityObserver)

        GlobalScope.launch { downloadJobItemWorkQueue.start() }
    }

    protected open fun onDownloadJobItemStarted(downloadJobItem: DownloadJobItem) {

    }

    protected open fun onDownloadQueueEmpty(lastDownloadJobItem: DownloadJobItem) {
        val currentConnectivityStatus = connectivityStatusRef.value
        if(currentConnectivityStatus != null &&
                currentConnectivityStatus.connectivityState == ConnectivityStatus.STATE_CONNECTED_LOCAL) {
            restoreWifi()
        }
    }

    /**
     * Check if the device can create BLE service and advertise it to the peer devices
     * @return true if can advertise its service else false
     */
    abstract fun canDeviceAdvertise(): Boolean


    /**
     * This should be called by the platform implementation when BLE discovers a nearby device
     * @param node The nearby device discovered
     */
    @Synchronized
    fun handleNodeDiscovered(node: NetworkNode) {
        GlobalScope.launch {
            withContext(singleThreadDispatcher) {
                localAvailabilityManager.handleNodeDiscovered(node.bluetoothMacAddress ?: "")
            }
        }
    }

    abstract fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle

    /**
     * Open bluetooth setting section from setting panel
     */
    abstract fun openBluetoothSettings()

    /**
     * Enable or disable WiFi on the device
     *
     * @param enabled Enable when true otherwise disable
     * @return true if the operation is successful, false otherwise
     */
    abstract fun setWifiEnabled(enabled: Boolean): Boolean

    /**
     * Get all peer network nodes that we know about
     * @param networkNodes Known NetworkNode
     * @return List of all known nodes
     */
    private fun getAllKnownNetworkNodeIds(networkNodes: List<NetworkNode>): List<Long> {
        val nodeIdList = ArrayList<Long>()
        for (networkNode in networkNodes) {
            nodeIdList.add(networkNode.nodeId)
        }
        return nodeIdList
    }

    /**
     * Connecting a client to a group network for content acquisition
     * @param ssid Group network SSID
     * @param passphrase Group network passphrase
     */
    abstract fun connectToWiFi(ssid: String, passphrase: String, timeout: Int)

    fun connectToWiFi(ssid: String, passphrase: String) {
        connectToWiFi(ssid, passphrase, DEFAULT_WIFI_CONNECTION_TIMEOUT)
    }

    /**
     * Restore the 'normal' WiFi connection
     */
    abstract fun restoreWifi()


    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param context Platform specific mContext
     * @param entryUidsToCheck List of entries to be checked from the peer device
     * @param peerToCheck Peer device to request from
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    abstract suspend fun makeEntryStatusTask(context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode): BleEntryStatusTask?

    /**
     * Create entry status task for a specific peer device,
     * it will request status of the provided entries from the provided peer device
     * @param context Platform specific mContext
     * @param message Message to be sent to the peer device
     * @param peerToSendMessageTo Peer device to send message to.
     * @param responseListener Message response listener object
     * @return Created BleEntryStatusTask
     *
     * @see BleEntryStatusTask
     */
    abstract fun makeEntryStatusTask(context: Any, message: BleMessage,
                                     peerToSendMessageTo: NetworkNode,
                                     responseListener: BleMessageResponseListener): BleEntryStatusTask?

    abstract fun makeDeleteJobTask(`object`: Any?, args: Map<String, String>): DeleteJobTaskRunner

    /**
     * Send message to a specific device
     * @param context Platform specific context
     * @param message Message to be send
     * @param peerToSendMessageTo Peer device to receive the message
     * @param responseListener Message response listener object
     */
    fun sendMessage(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode,
                    responseListener: BleMessageResponseListener) {
        makeEntryStatusTask(context, message, peerToSendMessageTo, responseListener)?.sendRequest()
    }


    /**
     * Cancel all download set and set items
     * @param downloadJobUid The download job uid that should be canceled and deleted
     */
    suspend fun cancelAndDeleteDownloadJob(downloadJobUid: Int) {
        umAppDatabase.downloadJobDao.updateJobAndItems(downloadJobUid,
                JobStatus.CANCELED, -1, JobStatus.CANCELED)
        val taskArgs = HashMap<String, String>()
        taskArgs[DeleteJobTaskRunner.ARG_DOWNLOAD_JOB_UID] = downloadJobUid.toString()

        makeDeleteJobTask(context, taskArgs).run()
    }

    //testing purpose only
    fun clearHistories() {
        locallyAvailableContainerUids.clear()
        knownPeerNodes.clear()
    }

    /**
     * Used for unit testing purposes only.
     *
     * @hide
     * @param database
     */
    fun setDatabase(database: UmAppDatabase) {
        this.umAppDatabase = database
    }

    open fun lockWifi(lockHolder: Any) {
        wifiLockHolders.add(lockHolder)
    }

    open fun releaseWifiLock(lockHolder: Any) {
        wifiLockHolders.remove(lockHolder)
    }

    /**
     * Handle node connection history, delete node which failed to connect for over 5 attempts
     * @param bluetoothAddress node bluetooth address
     * @param success connection status , True if the connection was made successfully,
     * otherwise false
     */
    fun handleNodeConnectionHistory(bluetoothAddress: String, success: Boolean) {
        var record: Int = knownBadNodeTrackList[bluetoothAddress] ?: 0

        if (success) {
            knownBadNodeTrackList[bluetoothAddress] = 0
            UMLog.l(UMLog.DEBUG, 694,
                    "Connection succeeded bad node counter was set to 0 for $bluetoothAddress")
        }

        if (!success) {
            knownBadNodeTrackList[bluetoothAddress] = record++
            UMLog.l(UMLog.DEBUG, 694,
                    "Connection failed and bad node counter set to $record for $bluetoothAddress")
        }

        if ((knownBadNodeTrackList[bluetoothAddress] ?: 0) > 5) {
            UMLog.l(UMLog.DEBUG, 694,
                    "Bad node counter exceeded threshold (5), removing node with address "
                            + bluetoothAddress + " from the list")
            knownBadNodeTrackList.remove(bluetoothAddress)
            knownPeerNodes.remove(bluetoothAddress)
            umAppDatabase.networkNodeDao.deleteByBluetoothAddress(bluetoothAddress)

            UMLog.l(UMLog.DEBUG, 694, "Node with address "
                    + bluetoothAddress + " removed from the list")
        }
    }

    /**
     * Get bad node by bluetooth address
     * @param bluetoothAddress node bluetooth address
     * @return bad node
     */
    fun getBadNodeTracker(bluetoothAddress: String): Int? {
        return knownBadNodeTrackList[bluetoothAddress]
    }

    fun isEntryLocallyAvailable(containerUid: Long): Boolean {
        return locallyAvailableContainerUids.contains(containerUid)
    }

    fun getLocallyAvailableContainerUids(): Set<Long> {
        return locallyAvailableContainerUids
    }


    /**
     * Clean up the network manager for shutdown
     */
    open fun onDestroy() {
        nextDownloadItemsLiveData.removeObserver(downloadQueueLocalAvailabilityObserver)
        val downloadQueueMonitorRequest = downloadQueueLocalAvailabilityObserver.currentRequest
        if(downloadQueueMonitorRequest != null)
            localAvailabilityManager.removeMonitoringRequest(downloadQueueMonitorRequest)

        entryStatusTaskExecutor.stop()
    }

    /**
     * Inserts a DownloadJob into the database for a given
     *
     * @param newDownloadJob the new DownloadJob to be created (with properties set)
     *
     * @return
     */
    suspend fun createNewDownloadJobItemManager(newDownloadJob: DownloadJob): DownloadJobItemManager {
        return jobItemManagerList!!.createNewDownloadJobItemManager(newDownloadJob)
    }

    suspend fun createNewDownloadJobItemManager(rootContentEntryUid: Long): DownloadJobItemManager {
        return createNewDownloadJobItemManager(DownloadJob(rootContentEntryUid,
                getSystemTimeInMillis()))
    }


    fun getDownloadJobItemManager(downloadJobId: Int): DownloadJobItemManager? {
        return jobItemManagerList!!.getDownloadJobItemManager(downloadJobId)
    }

    suspend fun openDownloadJobItemManager(downloadJobUid: Int) = jobItemManagerList!!.openDownloadJobItemManager(downloadJobUid)

    override suspend fun findDownloadJobItemStatusByContentEntryUid(contentEntryUid: Long) = jobItemManagerList!!.findDownloadJobItemStatusByContentEntryUid(contentEntryUid)

    override fun addDownloadChangeListener(listener: OnDownloadJobItemChangeListener) = jobItemManagerList!!.addDownloadChangeListener(listener)


    override fun removeDownloadChangeListener(listener: OnDownloadJobItemChangeListener) = jobItemManagerList!!.removeDownloadChangeListener(listener)


    companion object {

        /**
         * Convert decimal representation of an ip address back to IPV4 format.
         * @param ip decimal representation
         * @return IPV4 address
         */
        fun convertIpAddressToString(ip: Int): String {
            return ((ip shr 24 and 0xFF).toString() + "." + (ip shr 16 and 0xFF) + "."
                    + (ip shr 8 and 0xFF) + "." + (ip and 0xFF))
        }


        /**
         * Convert IP address to decimals
         * @param address IPV4 address
         * @return decimal representation of an IP address
         */
        fun convertIpAddressToInteger(address: String): Int {
            var result = 0
            val ipAddressInArray = address.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in 3 downTo 0) {
                val ip = ipAddressInArray[3 - i].toInt()
                result = result or (ip shl i * 8)
            }
            return result
        }

        protected var knownBadNodeTrackList: MutableMap<String, Int> = sharedMutableMapOf()

        /**
         * Flag to indicate entry status request
         */
        const val ENTRY_STATUS_REQUEST = 111.toByte()

        /**
         * Flag to indicate entry status response
         */
        const val ENTRY_STATUS_RESPONSE = 112.toByte()

        /**
         * Flag to indicate WiFi direct group request (for content download)
         */
        const val WIFI_GROUP_REQUEST = 113.toByte()

        /**
         * Flag to indicate WiFi direct group creation response
         */
        const val WIFI_GROUP_CREATION_RESPONSE = 114.toByte()


        /**
         * Commonly used MTU size for android devices
         */
        const val MINIMUM_MTU_SIZE = 20

        /**
         * Maximum MTU size for the packet transfer
         */
        const val MAXIMUM_MTU_SIZE = 512

        /**
         * Bluetooth Low Energy service UUID for our app
         */
        const val USTADMOBILE_BLE_SERVICE_UUID = "7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93a"

        /**
         * Peer WIFi direct group prefix
         */
        const val WIFI_DIRECT_GROUP_SSID_PREFIX = "DIRECT-"

        /**
         * Default timeout to wait for WiFi connection
         */
        const val DEFAULT_WIFI_CONNECTION_TIMEOUT = 60 * 1000
    }

}
