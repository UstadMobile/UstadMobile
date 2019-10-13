package com.ustadmobile.sharedse.network

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Handler
import android.os.Looper.getMainLooper
import android.os.ParcelUuid
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.net.ConnectivityManagerCompat
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil.normalizeAndroidWifiSsid
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.DownloadJobItem
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import com.ustadmobile.port.sharedse.util.AsyncServiceManager
import fi.iki.elonen.NanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * This class provides methods to perform android network related communications.
 * All Bluetooth Low Energy and WiFi direct communications will be handled here.
 * Also, this is maintained as a singleton by all activities binding to NetworkServiceAndroid,
 * which is responsible to call the onCreate method of this class.
 *
 * **Note:** Most of the scan / advertise methods here require
 * [android.Manifest.permission.BLUETOOTH_ADMIN] permission.
 *
 * @see NetworkManagerBle
 *
 *
 * @author kileha3
 */
actual open class NetworkManagerBle
/**
 * Constructor to be used when creating new instance
 *
 * @param context Platform specific application context
 */
actual constructor(context: Any, singleThreadDispatcher: CoroutineDispatcher)
    : NetworkManagerBleCommon(context, singleThreadDispatcher, Dispatchers.Main), EmbeddedHTTPD.ResponseListener {

    constructor(context: Any, singleThreadDispatcher: CoroutineDispatcher, httpd: EmbeddedHTTPD) : this(context, singleThreadDispatcher) {
        this.httpd = httpd
    }

    lateinit var httpd: EmbeddedHTTPD

    private lateinit var wifiManager: WifiManager

    private var bluetoothManager: Any? = null

    private var bluetoothAdapter: BluetoothAdapter? = null

    private var bleServiceAdvertiser: Any? = null

    private var bleScanCallback: Any? = null

    /* Cast as required to avoid ClassNotFoundException on Android versions that dont support this */
    private var gattServerAndroid: Any? = null

    private val mContext: Context = context as Context

    private val parcelServiceUuid = ParcelUuid(UUID.fromString(USTADMOBILE_BLE_SERVICE_UUID))

    private var wifiP2pChannel: WifiP2pManager.Channel? = null

    private var wifiP2pManager: WifiP2pManager? = null

    private var connectivityManager: ConnectivityManager? = null

    private val wifiP2PCapable = AtomicBoolean(false)

    private val wifiLockReference = AtomicReference<WifiManager.WifiLock>()

    private var wifiP2pGroupServiceManager: WifiP2PGroupServiceManager? = null

    private lateinit var managerHelper : NetworkManagerBleHelper

    @Volatile
    private var bleAdvertisingLastStartTime: Long = 0

    private val wifiDirectGroupLastRequestedTime = AtomicLong()

    private val wifiDirectRequestLastCompletedTime = AtomicLong()

    private val numActiveRequests = AtomicInteger()


    init {
        startMonitoringNetworkChanges()
    }

    /**
     * Receiver to handle bluetooth state changes
     */
    private val mBluetoothAndWifiStateChangeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkP2PBleServices()
        }
    }


    private val delayedExecutor = Executors.newSingleThreadScheduledExecutor()

    private val scanningServiceManager = object : AsyncServiceManager(
            STATE_STOPPED,
            { runnable, delay -> delayedExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS) }) {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun start() {
            if (isBleCapable) {
                UMLog.l(UMLog.DEBUG, 689,
                        "Starting BLE scanning")
                notifyStateChanged(STATE_STARTED)
                bluetoothAdapter!!.startLeScan(arrayOf(parcelServiceUuid.uuid),
                        bleScanCallback as BluetoothAdapter.LeScanCallback?)
                UMLog.l(UMLog.DEBUG, 689,
                        "BLE Scanning started ")
            } else {
                notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
                UMLog.l(UMLog.ERROR, 689,
                        "Not BLE capable, no need to start")
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        override fun stop() {
            bluetoothAdapter!!.stopLeScan(bleScanCallback as BluetoothAdapter.LeScanCallback?)
            notifyStateChanged(STATE_STOPPED)
        }
    }

    private val advertisingServiceManager = object : AsyncServiceManager(
            STATE_STOPPED,
            { runnable, delay -> delayedExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS) }) {
        override fun start() {
            if (canDeviceAdvertise()) {
                UMLog.l(UMLog.DEBUG, 689,
                        "Starting BLE advertising service")
                gattServerAndroid = BleGattServer(mContext,
                        this@NetworkManagerBle)
                bleServiceAdvertiser = bluetoothAdapter!!.bluetoothLeAdvertiser

                val service = BluetoothGattService(parcelServiceUuid.uuid,
                        BluetoothGattService.SERVICE_TYPE_PRIMARY)

                val writeCharacteristic = BluetoothGattCharacteristic(
                        parcelServiceUuid.uuid, BluetoothGattCharacteristic.PROPERTY_WRITE,
                        BluetoothGattCharacteristic.PERMISSION_WRITE)

                service.addCharacteristic(writeCharacteristic)
                if (gattServerAndroid == null
                        || (gattServerAndroid as BleGattServer).gattServer == null
                        || bleServiceAdvertiser == null) {
                    notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
                    return
                }

                (gattServerAndroid as BleGattServer).gattServer!!.addService(service)

                val settings = AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                        .setConnectable(true)
                        .setTimeout(0)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                        .build()

                val data = AdvertiseData.Builder()
                        .addServiceUuid(parcelServiceUuid).build()

                (bleServiceAdvertiser as BluetoothLeAdvertiser).startAdvertising(settings, data,
                        object : AdvertiseCallback() {
                            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                                super.onStartSuccess(settingsInEffect)
                                bleAdvertisingLastStartTime = System.currentTimeMillis()
                                notifyStateChanged(STATE_STARTED)
                                UMLog.l(UMLog.DEBUG, 689,
                                        "Service advertised successfully")
                            }

                            override fun onStartFailure(errorCode: Int) {
                                super.onStartFailure(errorCode)
                                notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
                                UMLog.l(UMLog.ERROR, 689,
                                        "Service could'nt start, with error code $errorCode")
                            }
                        })
            } else {
                notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
            }
        }

        override fun stop() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    val mGattServer = (gattServerAndroid as BleGattServer).gattServer
                    mGattServer!!.clearServices()
                    mGattServer.close()
                }
                gattServerAndroid = null
            } catch (e: Exception) {
                //maybe because bluetooth is actually off?
                UMLog.l(UMLog.ERROR, 689,
                        "Exception trying to stop gatt server", e)
            }

            notifyStateChanged(STATE_STOPPED)
        }
    }


    /**
     * Handle network state change events for android version < Lollipop
     */
    private val networkStateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val info = connectivityManager!!.activeNetworkInfo
            if (info != null && info.isConnected) {
                handleNetworkAvailable(null)
            } else {
                handleDisconnected()
            }
        }
    }


    /**
     * Check if the device needs runtime-permission
     * @return True if needed else False
     */
    private val isBleDeviceSDKVersion: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2


    /**
     * Check if the device can advertise BLE service
     * @return True if can advertise else false
     */
    private val isAdvertiser: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    private class WifiDirectGroupAndroid(group: WifiP2pGroup, endpointPort: Int) : WiFiDirectGroupBle(group.networkName, group.passphrase) {
        init {
            port = endpointPort
            ipAddress = "192.168.49.1"
        }
    }

    private class WifiP2PGroupServiceManager(private val networkManager: NetworkManagerBle) : AsyncServiceManager(STATE_STOPPED, { runnable, delay -> networkManager.delayedExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS) }) {

        private val wiFiDirectGroup = AtomicReference<WiFiDirectGroupBle>()

        private val timeoutCheckHandler = Handler()

        //it's working on it, and hasn't failed yet, don't notify status change
        val wifiP2pBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                networkManager.wifiP2pManager!!.requestGroupInfo(
                        networkManager.wifiP2pChannel) { group ->
                    wiFiDirectGroup.set(if (group != null)
                        WifiDirectGroupAndroid(group,
                                networkManager.httpd.listeningPort)
                    else
                        null)
                    if (group == null && state == STATE_STARTING || group != null && state == STATE_STOPPING) {
                        return@requestGroupInfo
                    }

                    notifyStateChanged(if (group != null) STATE_STARTED else STATE_STOPPED)
                }
            }
        }

        val group: WiFiDirectGroupBle
            get() = wiFiDirectGroup.get()

        private inner class CheckTimeoutRunnable : Runnable {
            override fun run() {
                val timeNow = System.currentTimeMillis()
                val timedOut = (networkManager.numActiveRequests.get() == 0
                        && timeNow - networkManager.wifiDirectGroupLastRequestedTime.get() > TIMEOUT_AFTER_GROUP_CREATION
                        && timeNow - networkManager.wifiDirectRequestLastCompletedTime.get() > TIMEOUT_AFTER_LAST_REQUEST)
                setEnabled(!timedOut)

                if (state != STATE_STOPPED)
                    timeoutCheckHandler.postDelayed(CheckTimeoutRunnable(),
                            TIMEOUT_CHECK_INTERVAL.toLong())
            }
        }

        override fun start() {
            timeoutCheckHandler.postDelayed(CheckTimeoutRunnable(), TIMEOUT_CHECK_INTERVAL.toLong())
            networkManager.wifiP2pManager!!.requestGroupInfo(networkManager.wifiP2pChannel
            ) { wifiP2pGroup ->
                if (wifiP2pGroup != null) {
                    wiFiDirectGroup.set(WifiDirectGroupAndroid(wifiP2pGroup,
                            networkManager.httpd.listeningPort))
                    notifyStateChanged(STATE_STARTED)
                } else {
                    createNewGroup()
                }
            }
        }

        private fun createNewGroup() {
            networkManager.wifiP2pManager!!.createGroup(networkManager.wifiP2pChannel,
                    object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            UMLog.l(UMLog.INFO, 692, "Group created successfully")
                            /* wait for the broadcast. OnSuccess might be called before the group is really ready */
                        }

                        override fun onFailure(reason: Int) {
                            UMLog.l(UMLog.ERROR, 692,
                                    "Failed to create a group with error code $reason")
                            notifyStateChanged(STATE_STOPPED, STATE_STOPPED)
                        }
                    })
        }

        override fun stop() {
            networkManager.wifiP2pManager!!.removeGroup(
                    networkManager.wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    UMLog.l(UMLog.INFO, 693,
                            "Group removed successfully")
                    wiFiDirectGroup.set(null)
                    notifyStateChanged(STATE_STOPPED)
                }

                override fun onFailure(reason: Int) {
                    UMLog.l(UMLog.ERROR, 693,
                            "Failed to remove a group with error code $reason")

                    //check if the group is still active
                    networkManager.wifiP2pManager!!.requestGroupInfo(
                            networkManager.wifiP2pChannel
                    ) { wifiP2pGroup ->
                        if (wifiP2pGroup != null) {
                            wiFiDirectGroup.set(WifiDirectGroupAndroid(wifiP2pGroup,
                                    networkManager.httpd.listeningPort))
                            notifyStateChanged(STATE_STARTED, STATE_STARTED)
                        } else {
                            wiFiDirectGroup.set(null)
                            notifyStateChanged(STATE_STOPPED)
                        }
                    }
                }
            })
        }

        companion object {

            private const val TIMEOUT_AFTER_GROUP_CREATION = 2 * 60 * 1000

            private const val TIMEOUT_AFTER_LAST_REQUEST = 30 * 1000

            private const val TIMEOUT_CHECK_INTERVAL = 30 * 1000
        }
    }


    /**
     * Callback for the network connectivity changes for android version >= Lollipop
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private inner class UmNetworkCallback : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            handleNetworkAvailable(network)
        }


        override fun onLost(network: Network) {
            super.onLost(network)
            UMLog.l(UMLog.VERBOSE, 42, "NetworkCallback: onAvailable" + prettyPrintNetwork(connectivityManager!!.getNetworkInfo(network)))
            handleDisconnected()
        }

        override fun onUnavailable() {
            UMLog.l(UMLog.VERBOSE, 42, "NetworkCallback: onUnavailable")
            super.onUnavailable()
            handleDisconnected()
        }
    }


    private fun handleDisconnected() {
        localHttpClient?.close()
        UMLog.l(UMLog.VERBOSE, 42, "NetworkCallback: handleDisconnected")
        connectivityStatusRef.value = ConnectivityStatus(ConnectivityStatus.STATE_DISCONNECTED,
                false, null)
        GlobalScope.launch {
            addLogs("changed to ${ConnectivityStatus.STATE_DISCONNECTED}, updating DB")
            umAppDatabase.connectivityStatusDao
                    .updateStateAsync(ConnectivityStatus.STATE_DISCONNECTED)
        }
    }


    private fun handleNetworkAvailable(network: Network?) {

        val isMeteredConnection = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager!!)
        val state = if (isMeteredConnection)
            ConnectivityStatus.STATE_METERED
        else
            ConnectivityStatus.STATE_UNMETERED

        val networkInfo: NetworkInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager!!.getNetworkInfo(network)
        } else {
            connectivityManager!!.activeNetworkInfo
        }

        UMLog.l(UMLog.VERBOSE, 42, "NetworkCallback: onAvailable" + prettyPrintNetwork(networkInfo))

        val ssid = if (networkInfo != null) normalizeAndroidWifiSsid(networkInfo.extraInfo) else null
        val status = ConnectivityStatus(state, true,
                ssid)
        addLogs("changed to $state")
        connectivityStatusRef.value = status

        //get network SSID
        if (ssid != null && ssid.startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX)) {
            status.connectivityState = ConnectivityStatus.STATE_CONNECTED_LOCAL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //only on main thread
                //first - check if the old client exists and close it


                if(localHttpClient == null){
                    localHttpClient = HttpClient(OkHttp) {
                        engine {
                            config {
                                socketFactory(network!!.socketFactory)
                            }
                        }
                        install(JsonFeature)
                    }
                }
            }
        }

        GlobalScope.launch {
            addLogs("changed to ${status.connectivityState}, updating DB =$umAppDatabase")
            umAppDatabase.connectivityStatusDao.insertAsync(status)
        }
    }


    private fun addLogs(message : String){
        println("NetworkConnectivityStatus: $message")
    }


    private fun prettyPrintNetwork(networkInfo: NetworkInfo?): String {
        var `val` = "Network : "
        if (networkInfo != null) {
            `val` += " type: " + networkInfo.typeName
            `val` += " extraInfo: " + networkInfo.extraInfo
        } else {
            `val` += " (null network info)"
        }

        return `val`
    }


    override fun onCreate() {
        wifiManager = managerHelper.wifiManager

        if (wifiP2pManager == null) {
            wifiP2pManager = mContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager?
        }
        wifiP2PCapable.set(wifiP2pManager != null)
        wifiP2pGroupServiceManager = WifiP2PGroupServiceManager(this)


        if (wifiP2PCapable.get()) {
            wifiP2pChannel = wifiP2pManager!!.initialize(mContext, getMainLooper(), null)
            mContext.registerReceiver(wifiP2pGroupServiceManager!!.wifiP2pBroadcastReceiver,
                    IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
        }

        if (isBleDeviceSDKVersion && isBleCapable) {

            bleScanCallback = BluetoothAdapter.LeScanCallback { device, _, _ ->
                val networkNode = NetworkNode()
                networkNode.bluetoothMacAddress = device.address
                handleNodeDiscovered(networkNode)
            }

            //setting up bluetooth connection listener
            val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            mContext.registerReceiver(mBluetoothAndWifiStateChangeBroadcastReceiver, intentFilter)

            if (Build.VERSION.SDK_INT > BLE_MIN_SDK_VERSION) {
                bluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE)
                bluetoothAdapter = (bluetoothManager as BluetoothManager).adapter
            }

            /**
             * Android will not send an initial state on startup as happens for most other
             * receivers, so we have to do that ourselves
             */
            if (bluetoothAdapter != null) {
                val initialBluetoothIntent = Intent(BluetoothAdapter.ACTION_STATE_CHANGED)
                initialBluetoothIntent.putExtra(BluetoothAdapter.EXTRA_STATE,
                        bluetoothAdapter!!.state)
                mBluetoothAndWifiStateChangeBroadcastReceiver.onReceive(mContext, initialBluetoothIntent)
            }
        }

        super.onCreate()
    }

    override fun onDownloadJobItemStarted(downloadJobItem: DownloadJobItem) {
        super.onDownloadJobItemStarted(downloadJobItem)

        val prepareJobIntent = Intent(mContext, DownloadNotificationService::class.java)
        prepareJobIntent.action = DownloadNotificationService.ACTION_DOWNLOADJOBITEM_STARTED
        prepareJobIntent.putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBITEMUID,
                downloadJobItem.djiUid)
        prepareJobIntent.putExtra(DownloadNotificationService.EXTRA_DOWNLOADJOBUID,
                downloadJobItem.djiDjUid)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(prepareJobIntent)
        } else {
            mContext.startService(prepareJobIntent)
        }
    }

    override fun responseStarted(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response) {
        if (session.remoteIpAddress != null && session.remoteIpAddress.startsWith("192.168.49")) {
            numActiveRequests.incrementAndGet()
        }
    }

    override fun responseFinished(session: NanoHTTPD.IHTTPSession, response: NanoHTTPD.Response?) {
        if (session.remoteIpAddress != null && session.remoteIpAddress.startsWith("192.168.49")) {
            numActiveRequests.decrementAndGet()
            wifiDirectGroupLastRequestedTime.set(System.currentTimeMillis())
        }
    }

    /**
     * Check that the required
     */
    fun checkP2PBleServices() {
        val permissionGranted = ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val scanningEnabled = permissionGranted && isBluetoothEnabled && isBleCapable && wifiManager.isWifiEnabled
        val advertisingEnabled = scanningEnabled and canDeviceAdvertise()
        var waitedLongEnoughToStartScanning = true
        val timeNow = System.currentTimeMillis()

        if (advertisingEnabled) {
            waitedLongEnoughToStartScanning = bleAdvertisingLastStartTime != 0L && timeNow - bleAdvertisingLastStartTime > BLE_SCAN_WAIT_AFTER_ADVERTISING
        }

        scanningServiceManager.setEnabled(scanningEnabled && waitedLongEnoughToStartScanning)
        advertisingServiceManager.setEnabled(advertisingEnabled)

        if (scanningEnabled && !waitedLongEnoughToStartScanning) {
            delayedExecutor.schedule({ this.checkP2PBleServices() },
                    (BLE_SCAN_WAIT_AFTER_ADVERTISING + 1000).toLong(), TimeUnit.MILLISECONDS)
        }
    }

    /**
     * {@inheritDoc}
     */
    actual override val isWiFiEnabled: Boolean
        get() = wifiManager.isWifiEnabled

    /**
     * {@inheritDoc}
     */
    actual override val isBleCapable: Boolean
        get() {
            return if (isBleDeviceSDKVersion)
                BluetoothAdapter.getDefaultAdapter() != null && mContext.packageManager
                        .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
            else
                false
        }


    /**
     * {@inheritDoc}
     */
    actual override val isBluetoothEnabled: Boolean
        get() = (bluetoothAdapter != null && bluetoothAdapter!!.isEnabled
                && bluetoothAdapter!!.state == BluetoothAdapter.STATE_ON)


    /**
     * {@inheritDoc}
     */
    actual override fun canDeviceAdvertise(): Boolean {
        return Build.VERSION.SDK_INT > BLE_ADVERTISE_MIN_SDK_VERSION &&
                (isAdvertiser && bluetoothAdapter != null
                        && bluetoothAdapter!!.isMultipleAdvertisementSupported)
    }

    /**
     * {@inheritDoc}
     */
    actual override fun openBluetoothSettings() {
        mContext.startActivity(Intent(
                android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    /**
     * {@inheritDoc}
     */
    actual override fun setWifiEnabled(enabled: Boolean): Boolean {
        return wifiManager.setWifiEnabled(enabled)
    }


    actual override fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle {
        wifiDirectGroupLastRequestedTime.set(System.currentTimeMillis())
        wifiP2pGroupServiceManager!!.setEnabled(true)
        wifiP2pGroupServiceManager!!.await({ state -> state == AsyncServiceManager.STATE_STARTED || state == AsyncServiceManager.STATE_STOPPED },
                timeout)
        return wifiP2pGroupServiceManager!!.group
    }


    /**
     * {@inheritDoc}
     */
    actual override fun connectToWiFi(ssid: String, passphrase: String, timeout: Int) {
        managerHelper.deleteTemporaryWifiDirectSsids()
        endAnyLocalSession()
        managerHelper.setGroupInfo(ssid,passphrase)

        val connectionDeadline = System.currentTimeMillis() + timeout

        var connectedOrFailed = false

        var networkEnabled = false

        do {
            UMLog.l(UMLog.INFO, 693, "Trying to connect to $ssid")
            if (!networkEnabled) {
                managerHelper.enableWifiNetwork()

                UMLog.l(UMLog.INFO, 693,
                        "Network changed  to $ssid")
                networkEnabled = true
            } else if (isConnectedToRequiredWiFi(ssid)) {
                UMLog.l(UMLog.INFO, 693,
                        "ConnectToWifi: Already connected to WiFi with ssid =$ssid")
                break
            } else {
                val routeInfo = wifiManager.dhcpInfo
                if (routeInfo != null && routeInfo.gateway > 0) {
                    @SuppressLint("DefaultLocale")
                    val gatewayIp = String.format("%d.%d.%d.%d",
                            routeInfo.gateway and 0xff,
                            routeInfo.gateway shr 8 and 0xff,
                            routeInfo.gateway shr 16 and 0xff,
                            routeInfo.gateway shr 24 and 0xff)
                    UMLog.l(UMLog.INFO, 693,
                            "Trying to ping gateway IP address $gatewayIp")
                    if (ping(gatewayIp, 1000)) {
                        UMLog.l(UMLog.INFO, 693,
                                "Ping successful!$ssid")
                        connectedOrFailed = true
                    } else {
                        UMLog.l(UMLog.INFO, 693,
                                "ConnectToWifi: ping to $gatewayIp failed on $ssid")
                    }
                } else {
                    UMLog.l(UMLog.INFO, 693,
                            "ConnectToWifi: No DHCP gateway yet on $ssid")
                }
            }


            if (!connectedOrFailed && System.currentTimeMillis() > connectionDeadline) {
                UMLog.l(UMLog.INFO, 693, " TIMEOUT: failed to connect $ssid")
                break
            }
            SystemClock.sleep(1000)

        } while (!connectedOrFailed)
    }

    private fun ping(ipAddress: String, timeout: Long): Boolean {
        try {
            return InetAddress.getByName(ipAddress).isReachable(timeout.toInt())
        } catch (e: IOException) {
            //ping did not succeed
        }

        return false
    }

    private fun isConnectedToRequiredWiFi(ssid: String): Boolean {
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo != null && normalizeAndroidWifiSsid(wifiInfo.ssid) == normalizeAndroidWifiSsid(ssid)
    }


    actual override fun restoreWifi() {
        UMLog.l(UMLog.INFO, 339, "NetworkManager: restore wifi")
        endAnyLocalSession()
        managerHelper.restoreWiFi()
    }

    /**
     * Send an http request to the server so it knows we are done
     */
    private fun endAnyLocalSession() {
        GlobalScope.launch {
            val currentConnectivityStatus = connectivityStatusRef.value
            val currentWifiSsid = currentConnectivityStatus?.wifiSsid
            if (currentConnectivityStatus == null
                    || currentWifiSsid == null
                    || !currentWifiSsid.startsWith(WIFI_DIRECT_GROUP_SSID_PREFIX))
                return@launch

            val endpoint = umAppDatabase.networkNodeDao.getEndpointUrlByGroupSsid(
                    currentWifiSsid)
            if (endpoint == null) {
                UMLog.l(UMLog.ERROR, 699,
                        "ERROR: No endpoint url for ssid$currentWifiSsid")
                return@launch
            }

            try {
                val endSessionUrl = endpoint + "endsession"
                localHttpClient?.get<Any>(endSessionUrl)
                UMLog.l(UMLog.INFO, 699, "Send end of session request $endSessionUrl")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }


    /**
     * {@inheritDoc}
     */
    actual override fun makeEntryStatusTask(context: Any?, entryUidsToCheck: List<Long>,
                                            peerToCheck: NetworkNode?): BleEntryStatusTask? {
        if (Build.VERSION.SDK_INT > BLE_MIN_SDK_VERSION) {
            val entryStatusTask = BleEntryStatusTaskAndroid(
                    context as Context, this, entryUidsToCheck, peerToCheck!!)
            entryStatusTask.setBluetoothManager(bluetoothManager as BluetoothManager)
            return entryStatusTask
        }
        return null
    }

    /**
     * {@inheritDoc}
     */
    actual override fun makeEntryStatusTask(context: Any, message: BleMessage,
                                            peerToSendMessageTo: NetworkNode,
                                            responseListener: BleMessageResponseListener): BleEntryStatusTask? {
        if (Build.VERSION.SDK_INT > BLE_MIN_SDK_VERSION) {
            val task = BleEntryStatusTaskAndroid(context as Context, this, message, peerToSendMessageTo, responseListener)
            task.setBluetoothManager(bluetoothManager as BluetoothManager)
            return task
        }
        return null
    }

    actual override fun makeDeleteJobTask(`object`: Any?, args: Map<String, String>): DeleteJobTaskRunner {
        return DeleteJobTaskRunnerAndroid(`object`, args)
    }


    /**
     * Start monitoring network changes
     */
    private fun startMonitoringNetworkChanges() {
        managerHelper = NetworkManagerBleHelper(mContext)
        connectivityManager = managerHelper.connectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val networkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .build()
            if (connectivityManager != null) {
                connectivityManager!!.requestNetwork(networkRequest, UmNetworkCallback())
            }
        } else {
            val connectionFilter = IntentFilter()
            connectionFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            connectionFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            mContext.registerReceiver(networkStateChangeReceiver, connectionFilter)
        }

    }

    /**
     * Get bluetooth manager instance
     * @return Instance of a BluetoothManager
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    internal fun getBluetoothManager(): BluetoothManager {
        return bluetoothManager as BluetoothManager
    }

    @VisibleForTesting
    fun setBluetoothManager(manager: BluetoothManager) {
        this.bluetoothManager = manager
    }

    override fun lockWifi(lockHolder: Any) {
        super.lockWifi(lockHolder)

        if (wifiLockReference.get() == null) {
            wifiLockReference.set(wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                    "UstadMobile-Wifi-Lock-Tag"))
            UMLog.l(UMLog.INFO, 699, "WiFi lock acquired for $lockHolder")
        }
    }

    override fun releaseWifiLock(lockHolder: Any) {
        super.releaseWifiLock(lockHolder)

        val lock = wifiLockReference.get()
        if (wifiLockHolders.isEmpty() && lock != null) {
            wifiLockReference.set(null)
            lock.release()
            UMLog.l(UMLog.ERROR, 699,
                    "WiFi lock released from object $lockHolder")
        }

    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        scanningServiceManager.setEnabled(false)
        advertisingServiceManager.setEnabled(false)
        wifiP2pGroupServiceManager!!.setEnabled(false)

        if (isBleCapable) {
            mContext.unregisterReceiver(mBluetoothAndWifiStateChangeBroadcastReceiver)
        }

        if (!isVersionLollipopOrAbove) {
            mContext.unregisterReceiver(networkStateChangeReceiver)
        }

        if (wifiP2PCapable.get()) {
            mContext.unregisterReceiver(wifiP2pGroupServiceManager!!.wifiP2pBroadcastReceiver)
        }

        super.onDestroy()
    }

    actual override val isVersionLollipopOrAbove: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    actual override val isVersionKitKatOrBelow: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP


    companion object {

        /**
         * When we use BLE for advertising and scanning, we need wait a little bit after one starts
         * before the other can start
         */
        const val BLE_SCAN_WAIT_AFTER_ADVERTISING = 4000

        val USTADMOBILE_BLE_SERVICE_UUID_UUID = UUID.fromString(USTADMOBILE_BLE_SERVICE_UUID)

        const val BLE_ADVERTISE_MIN_SDK_VERSION = 21

        const val BLE_MIN_SDK_VERSION = 18
    }
}
