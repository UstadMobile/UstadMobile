package com.ustadmobile.sharedse.network

import com.github.aakira.napier.Napier
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.sharedMutableMapOf
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import kotlin.collections.set
import kotlin.jvm.Synchronized

/**
 * This is an abstract class which is used to implement platform specific NetworkManager
 *
 *
 * @property context system context to use
 * @property singleThreadDispatcher A single thread based dispatcher that is used for tracking download
 *                                  status. ContainerDownloadManager requires a single thread environment
 *
 * @author kileha3
 */
abstract class NetworkManagerBleCommon(
        val context: Any = Any(),
        override val di: DI,
        private val singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default,
        private val mainDispatcher: CoroutineDispatcher = Dispatchers.Default,
        private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default) : DIAware {

    private val knownNodesLock = Any()

    private var isStopMonitoring = false

    private val networkNodeListeners: MutableList<NetworkNodeListener> = copyOnWriteListOf()

    private val knownNetworkNodes: MutableList<NetworkNode> = copyOnWriteListOf()

    val networkNodes: List<NetworkNode>
        get() = knownNetworkNodes.toList()

    /**
     * Android 5+ requires using a bound socketFactory from the network object to route traffic over
     * a WiFi network that has no Internet.
     *
     * The underlying implementation will create this HttpClient object when connecting to a network
     * with no Internet access.
     */
    var localHttpClient: HttpClient? = null
        protected set


    private val locallyAvailableContainerUids = mutableSetOf<Long>()

    protected var wifiLockHolders = mutableListOf<Any>()

    private val knownPeerNodes = mutableMapOf<String, Long>()

    private lateinit var nextDownloadItemsLiveData: DoorLiveData<List<DownloadJobItem>>

    protected val _connectivityStatus = DoorMutableLiveData<ConnectivityStatus>()

    val connectivityStatus: DoorLiveData<ConnectivityStatus>
        get() = _connectivityStatus

    //Was previously internal: this does not compile since Kotlin 1.3.61
    class DownloadQueueLocalAvailabilityObserver(val localAvailabilityManager: LocalAvailabilityManager): DoorObserver<List<DownloadJobItem>> {

        var currentRequest: AvailabilityMonitorRequest? = null

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

    abstract val isVersionLollipopOrAbove: Boolean

    abstract val isVersionKitKatOrBelow: Boolean



//    val localAvailabilityManager: LocalAvailabilityManagerImpl = LocalAvailabilityManagerImpl(context,
//            this::makeEntryStatusTask, singleThreadDispatcher,
//            this::onNewBleNodeDiscovered, this::onBleNodeLost,
//            this::onBleNodeReputationChanged,
//            umAppDatabase.locallyAvailableContainerDao)

    //private val downloadQueueLocalAvailabilityObserver = DownloadQueueLocalAvailabilityObserver(localAvailabilityManager)

    private val bleMirrorIdMap = mutableMapOf<String, Int>()

    abstract val localHttpPort: Int


    /**
     * Start web server, advertising and discovery
     */
    open fun onCreate() {
//        nextDownloadItemsLiveData = umAppDatabase.downloadJobItemDao.findNextDownloadJobItems()
//        nextDownloadItemsLiveData.observeForever(downloadQueueLocalAvailabilityObserver)
    }

    protected suspend fun onNewBleNodeDiscovered(bluetoothAddress: String) {
//        val dbRepo = (umAppDatabaseRepo as DoorDatabaseRepository)
//        val mirrorId = dbRepo.addMirror("http://127.0.0.1:${localHttpPort}/bleproxy/$bluetoothAddress/rest/",
//                100)
//        bleMirrorIdMap[bluetoothAddress] = mirrorId
    }

    protected suspend fun onBleNodeLost(bluetoothAddress: String) {
//        val dbRepo = (umAppDatabaseRepo as DoorDatabaseRepository)
//        val mirrorId = bleMirrorIdMap[bluetoothAddress] ?: 0
//        dbRepo.removeMirror(mirrorId)
//        Napier.v({"Removed mirror $mirrorId for $bluetoothAddress"})
    }

    protected suspend fun onBleNodeReputationChanged(bluetoothAddress: String, reputation: Int) {
//        val dbRepo = (umAppDatabaseRepo as DoorDatabaseRepository)
//        val mirrorId = bleMirrorIdMap[bluetoothAddress] ?: 0
//        dbRepo.updateMirrorPriorities(mapOf(mirrorId to reputation))
//        Napier.d({"Update mirror reputation for $mirrorId [$bluetoothAddress] to $reputation"})
    }


    protected open fun onDownloadJobItemStarted(downloadJobItem: DownloadJobItem) {

    }

    protected open fun onDownloadQueueEmpty() {
        val currentConnectivityStatus = _connectivityStatus.getValue()
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
        val knownNetworkNode = knownNetworkNodes.firstOrNull { it.bluetoothMacAddress == node.bluetoothMacAddress }
        if(knownNetworkNode == null) {
            Napier.i("NetworkManagerBle: Discovered new node on ${node.bluetoothMacAddress} ")
            knownNetworkNodes += node
            GlobalScope.launch {
                networkNodeListeners.forEach { it.onNewNodeDiscovered(node) }
            }
        }else {
            knownNetworkNode.lastUpdateTimeStamp = getSystemTimeInMillis()
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
     * Send message to a specific device
     * @param context Platform specific context
     * @param message Message to be send
     * @param peerToSendMessageTo Peer device to receive the message
     * @param responseListener Message response listener object
     */
    fun sendMessage(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode,
                    responseListener: BleMessageResponseListener) {

    }

    abstract suspend fun sendBleMessage(bleMessage: BleMessage, deviceAddr: String): BleMessage?

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
            //TODO: node should be stored in all stored databases
            //umAppDatabase.networkNodeDao.deleteByBluetoothAddress(bluetoothAddress)

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

    fun addNetworkNodeListener(listener: NetworkNodeListener) = networkNodeListeners.add(listener)

    fun removeNetworkNodeListener(listener: NetworkNodeListener) = networkNodeListeners.remove(listener)


    /**
     * Clean up the network manager for shutdown
     */
    open fun onDestroy() {
//        nextDownloadItemsLiveData.removeObserver(downloadQueueLocalAvailabilityObserver)
//        val downloadQueueMonitorRequest = downloadQueueLocalAvailabilityObserver.currentRequest
//        if(downloadQueueMonitorRequest != null)
//            localAvailabilityManager.removeMonitoringRequest(downloadQueueMonitorRequest)
    }


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

        val BLE_CHARACTERISTICS = listOf("7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93d",
                "7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93e", "7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93f",
                "7d2ea28a-f7bd-485a-bd9d-92ad6ecfe93b")

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
