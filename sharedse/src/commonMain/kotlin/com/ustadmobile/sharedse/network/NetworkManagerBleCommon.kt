package com.ustadmobile.sharedse.network

import io.github.aakira.napier.Napier
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.util.copyOnWriteListOf
import com.ustadmobile.lib.util.getSystemTimeInMillis
import com.ustadmobile.lib.util.sharedMutableMapOf
import io.ktor.client.HttpClient
import kotlinx.coroutines.*
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

    protected val _connectivityStatus = MutableLiveData<ConnectivityStatus>()

    val connectivityStatus: LiveData<ConnectivityStatus>
        get() = _connectivityStatus


    /**
     * Check if WiFi is enabled / disabled on the device
     * @return boolean true, if enabled otherwise false.
     */
    abstract val isWiFiEnabled: Boolean


    /**
     * Check if bluetooth is enabled on the device
     * @return True if enabled otherwise false
     */
    abstract val isBluetoothEnabled: Boolean


    //private val downloadQueueLocalAvailabilityObserver = DownloadQueueLocalAvailabilityObserver(localAvailabilityManager)

    private val bleMirrorIdMap = mutableMapOf<String, Int>()

    abstract val localHttpPort: Int


    /**
     * Start web server, advertising and discovery
     */
    open fun onCreate() {

    }


    /**
     * Open bluetooth setting section from setting panel
     */
    abstract fun openBluetoothSettings()


    open fun lockWifi(lockHolder: Any) {
        wifiLockHolders.add(lockHolder)
    }

    open fun releaseWifiLock(lockHolder: Any) {
        wifiLockHolders.remove(lockHolder)
    }


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

        /**
         * The timeout after which, if a BLE node has not been heard from, it will be considered lost
         */
        const val BLE_NODE_TIMEOUT = 10000
    }

}
