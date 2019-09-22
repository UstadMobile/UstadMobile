package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityListener
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.networkmanager.OnDownloadJobItemChangeListener
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
        private val mainDispatcher: CoroutineDispatcher = Dispatchers.Default) : LocalAvailabilityMonitor,
        DownloadJobItemStatusProvider {

    private val knownNodesLock = Any()

    private var isStopMonitoring = false


    private val availabilityMonitoringRequests = mutableMapOf<Any, List<Long>>()

    /**
     * @return Active http client
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

    private lateinit var umAppDatabase: UmAppDatabase

    private lateinit var umAppDatabaseRepo: UmAppDatabase

    private val localAvailabilityListeners = copyOnWriteListOf<LocalAvailabilityListener>()

    private var jobItemManagerList: DownloadJobItemManagerList? = null

    private val entryStatusTaskExecutor = EntryTaskExecutor(5)


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
        umAppDatabase = UmAppDatabase.getInstance(context)
        umAppDatabaseRepo = umAppDatabase
        jobItemManagerList = DownloadJobItemManagerList(umAppDatabase, singleThreadDispatcher)
        downloadJobItemWorkQueue = LiveDataWorkQueue(umAppDatabase.downloadJobItemDao.findNextDownloadJobItems(),
                { item1, item2 -> item1.djiUid == item2.djiUid }, mainDispatcher = mainDispatcher) {
            DownloadJobItemRunner(context, it, this@NetworkManagerBleCommon,
                    umAppDatabase, umAppDatabaseRepo, UmAccountManager.getActiveEndpoint(context)!!,
                    connectivityStatusRef.value, mainCoroutineDispatcher = mainDispatcher).download()
        }
        GlobalScope.launch { downloadJobItemWorkQueue.start() }
    }

    /*override */fun onQueueEmpty() {
//        if (connectivityStatusRef.get() != null && connectivityStatusRef.get().connectivityState == ConnectivityStatus.STATE_CONNECTED_LOCAL) {
//            Thread(Runnable { this.restoreWifi() }).start()
//        }
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
        val networkNodeDao = umAppDatabase.networkNodeDao

        if (!knownPeerNodes.containsKey(node.bluetoothMacAddress)) {

            node.lastUpdateTimeStamp = getSystemTimeInMillis()

            GlobalScope.launch {
                val result = networkNodeDao.updateLastSeenAsync(node.bluetoothMacAddress!!,
                        node.lastUpdateTimeStamp)
                knownPeerNodes[node.bluetoothMacAddress!!] = node.lastUpdateTimeStamp
                if (result == 0) {
                    networkNodeDao.insertAsync(node)
                    UMLog.l(UMLog.DEBUG, 694, "New node with address "
                            + node.bluetoothMacAddress + " found, added to the Db")

                    val entryUidsToMonitor = ArrayList(allUidsToBeMonitored)

                    if (!isStopMonitoring) {
                        if (entryUidsToMonitor.size > 0) {
                            val entryStatusTask = makeEntryStatusTask(context, entryUidsToMonitor, node)
                            entryStatusTasks.add(entryStatusTask!!)
                            entryStatusTaskExecutor.execute(entryStatusTask)
                        }
                    }
                }


            }
        } else {
            val lastSeenInMills = getSystemTimeInMillis()
            val canUpdate = (lastSeenInMills - knownPeerNodes[node.bluetoothMacAddress!!]!!) >= (20 * 1000).toLong()
            if(canUpdate){
                val nodeMap = HashMap(knownPeerNodes)
                knownPeerNodes[node.bluetoothMacAddress!!] = lastSeenInMills
                GlobalScope.launch {
                    umAppDatabase.networkNodeDao.updateNodeLastSeen(nodeMap)
                    nodeMap.clear()
                    UMLog.l(UMLog.DEBUG, 694, "Updating "
                            + knownPeerNodes.size + " nodes from the Db")
                }
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
     * Start monitoring availability of specific entries from peer devices
     * @param monitor Object to monitor e.g Presenter
     * @param entryUidsToMonitor List of entries to be monitored
     */
    override fun startMonitoringAvailability(monitor: Any, entryUidsToMonitor: List<Long>) {
        try {
            isStopMonitoring = false
            availabilityMonitoringRequests[monitor] = entryUidsToMonitor
            UMLog.l(UMLog.DEBUG, 694, "Registered a monitor with "
                    + entryUidsToMonitor.size + " entry(s) to be monitored")

            val networkNodeDao = umAppDatabase.networkNodeDao
            val responseDao = umAppDatabase.entryStatusResponseDao

            val lastUpdateTime = getSystemTimeInMillis() - (60 * 1000)

            val uniqueEntryUidsToMonitor = ArrayList(allUidsToBeMonitored)
            val knownNetworkNodes = getAllKnownNetworkNodeIds(networkNodeDao.findAllActiveNodes(lastUpdateTime, 1))

            UMLog.l(UMLog.DEBUG, 694,
                    "Found total of   " + uniqueEntryUidsToMonitor.size +
                            " uids to check their availability status")

            val entryWithoutRecentResponses = responseDao.findEntriesWithoutRecentResponse(
                    uniqueEntryUidsToMonitor, knownNetworkNodes,
                    getSystemTimeInMillis() - (2 * 60 * 1000))

            //Group entryUUid by node where their status will be checked from
            val nodeToCheckEntryList = LinkedHashMap<Int, MutableList<Long>>()

            for (entryResponse in entryWithoutRecentResponses) {

                val nodeIdToCheckFrom = entryResponse.nodeId
                if (!nodeToCheckEntryList.containsKey(nodeIdToCheckFrom))
                    nodeToCheckEntryList[nodeIdToCheckFrom] = arrayListOf()

                nodeToCheckEntryList[nodeIdToCheckFrom]!!.add(entryResponse.containerUid)
            }

            UMLog.l(UMLog.DEBUG, 694,
                    "Created total of  " + nodeToCheckEntryList.entries.size
                            + " entry(s) to be checked from")

            //Make entryStatusTask as per node list and entryUuids found
            for (nodeId in nodeToCheckEntryList.keys) {
                val networkNode = networkNodeDao.findNodeById(nodeId.toLong())
                val entryStatusTask = makeEntryStatusTask(context,
                        nodeToCheckEntryList[nodeId]!!, networkNode)
                entryStatusTasks.add(entryStatusTask!!)
                GlobalScope.launch {
                    entryStatusTaskExecutor.execute(entryStatusTask)
                }
                UMLog.l(UMLog.DEBUG, 694,
                        "Status check started for " + nodeToCheckEntryList[nodeId]!!.size
                                + " entry(s) task from " + networkNode!!.bluetoothMacAddress)
            }
        } catch (e: Exception) {
            UMLog.l(UMLog.ERROR,694,e.message,e)
        }

    }

    /**
     * Stop monitoring the availability of entries from peer devices
     * @param monitor Monitor object which created a monitor (e.g Presenter)
     */
    override fun stopMonitoringAvailability(monitor: Any) {
        availabilityMonitoringRequests.remove(monitor)
        isStopMonitoring = availabilityMonitoringRequests.isEmpty()
    }

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
    abstract fun makeEntryStatusTask(context: Any?, entryUidsToCheck: List<Long>, peerToCheck: NetworkNode?): BleEntryStatusTask?

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
        makeEntryStatusTask(context, message, peerToSendMessageTo, responseListener)?.run()
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


    /**
     * Add listener to the list of local availability listeners
     * @param listener listener object to be added.
     */
    fun addLocalAvailabilityListener(listener: LocalAvailabilityListener) {
        if (!localAvailabilityListeners.contains(listener)) {
            localAvailabilityListeners.add(listener)
        }
    }

    /**
     * Remove a listener from a list of all available listeners
     * @param listener listener to be removed
     */
    fun removeLocalAvailabilityListener(listener: LocalAvailabilityListener) {
        localAvailabilityListeners.remove(listener)
    }

    /**
     * Trigger availability status change event to all listening parts
     */
    private fun fireLocalAvailabilityChanged() {
        val listenerList = ArrayList(localAvailabilityListeners)
        for (listener in listenerList) {
            listener.onLocalAvailabilityChanged(locallyAvailableContainerUids)
        }
    }

    /**
     * All all availability statuses received from the peer node
     * @param responses response received
     */
    @Synchronized
    fun handleLocalAvailabilityResponsesReceived(responses: MutableList<EntryStatusResponse>) {
        if (responses.isEmpty())
            return

        val nodeId = responses[0].erNodeId
        if (!entryStatusResponses.containsKey(nodeId))
            entryStatusResponses[nodeId] = mutableListOf()

        entryStatusResponses[nodeId]!!.addAll(responses)
        locallyAvailableContainerUids.clear()

        for (responseList in entryStatusResponses.values) {
            for (response in responseList) {
                if (response.available)
                    locallyAvailableContainerUids.add(response.erContainerUid)
            }
        }

        fireLocalAvailabilityChanged()
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
        //downloadJobItemWorkQueue.shutdown();
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

    fun deleteUnusedDownloadJob(downloadJobUid: Int) {
        GlobalScope.launch { jobItemManagerList!!.deleteUnusedDownloadJob(downloadJobUid) }
    }

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
         * Commonly used MTU for android devices
         */
        const val DEFAULT_MTU_SIZE = 20

        /**
         * Maximum MTU for the packet transfer
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
        const val DEFAULT_WIFI_CONNECTION_TIMEOUT = 30 * 1000
    }

}
