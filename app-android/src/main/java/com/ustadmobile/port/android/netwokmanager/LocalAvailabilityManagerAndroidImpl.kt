package com.ustadmobile.port.android.netwokmanager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.github.aakira.napier.Napier
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.AvailabilityMonitorRequest
import com.ustadmobile.core.networkmanager.LocalAvailabilityManager
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.NetworkNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

fun BluetoothDevice.asNetworkNode(): NetworkNode {
    return NetworkNode().also {
        it.bluetoothMacAddress = this.address
        it.nodeName = this.name
    }
}

fun NetworkNode.makeCopy()= NetworkNode().also {
    it.nodeId = nodeId
    it.nodeName = nodeName
    it.bluetoothMacAddress = bluetoothMacAddress
    it.bluetoothBondState = bluetoothBondState
    it.groupSsid = groupSsid
}

class LocalAvailabilityManagerAndroidImpl(val endpoint: Endpoint, val appContext: Context,
                                          override val di: DI,
    val singleThreadContext : CoroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher()) :
        LocalAvailabilityManager, DIAware {

    private val networkNodesMap: MutableMap<Long, NetworkNode> = ConcurrentHashMap()

    private val networkNodesLiveDataInternal: DoorMutableLiveData<List<NetworkNode>> = DoorMutableLiveData(listOf())

    private val db: UmAppDatabase by on(endpoint).instance(tag = DoorTag.TAG_DB)

    override val networkNodesLiveData: DoorLiveData<List<NetworkNode>>
        get() = networkNodesLiveDataInternal

    private val btAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private val serviceUUIDObj : UUID by lazy {
        UUID(endpoint.url.hashCode().toLong(), endpoint.url.hashCode().toLong())
    }

    override val serviceUuid: String by lazy {
        serviceUUIDObj.toString()
    }

    override val serviceName: String by lazy {
        "um${endpoint.url.hashCode().toString().replace('-', '_')}"
    }

    inner class ServerAcceptThread(private val btAdapter: BluetoothAdapter): Thread() {

        var enabled = true

        private var serverSocket: BluetoothServerSocket? = null

        override fun run() {
            while(enabled) {
                try {
                    Napier.d("Bluetooth Server Starting for $serviceName UUID=$serviceUUIDObj", tag = LOGTAG)
                    serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(serviceName,
                            serviceUUIDObj)
                    serverSocket?.accept()?.run {
                        ServerClientThread(this).start()
                    }
                }catch(e: Exception) {
                    Napier.e("Server Socket Exception", tag = LOGTAG)
                }finally {
                    serverSocket?.close()
                }
            }
        }

        fun close() {
            enabled = false
            serverSocket?.close()
            serverSocket = null
        }
    }

    private var serverAcceptThread: ServerAcceptThread? = null

    private class ServerClientThread(private val client: BluetoothSocket) : Thread() {

    }

    internal val bluetoothStateBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val btState = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ?: -1
            val btAdapterVal = btAdapter ?: return
            when(btState) {
                BluetoothAdapter.STATE_ON -> {
                    Napier.d("Bluetooth Adapter On: start RFComm server for ${endpoint.url}", tag = LOGTAG)
                    if(serverAcceptThread == null) {
                        serverAcceptThread = ServerAcceptThread(btAdapterVal).also {
                            it.start()
                        }
                    }
                }

                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Napier.d("Bluetooth adapter off", tag = LOGTAG)
                    serverAcceptThread?.close()
                    serverAcceptThread = null
                }
            }
        }
    }


    internal val bluetoothFoundBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val deviceFound = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                    GlobalScope.launch(singleThreadContext) {
                        val knownDevice = findByBluetoothAddr(deviceFound.address)
                        if(knownDevice != null) {
                            onNodeUpdated(knownDevice.makeCopy().apply {
                                lastUpdateTimeStamp = systemTimeInMillis()
                                bluetoothBondState = deviceFound.bondState
                            })
                        }else {
                            onNewNodeDiscovered(deviceFound.asNetworkNode())
                        }
                    }
                }
            }
        }
    }

    override var bluetoothScanningEnabled: Boolean = false
        set(value) {
            if(value && !field) {
                val foundIntentFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                appContext.registerReceiver(bluetoothFoundBroadcastReceiver, foundIntentFilter)
                val discoveryFinishedFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                appContext.registerReceiver(bluetoothFoundBroadcastReceiver, discoveryFinishedFilter)

                btAdapter?.startDiscovery()
                Napier.d("Bluetooth discovery started", tag = LOGTAG)
            }else if(!value && field) {
                //stop scanning
                BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
            }

            field = value
        }


    init {
        appContext.registerReceiver(bluetoothStateBroadcastReceiver,
                IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        GlobalScope.launch(Dispatchers.Main) {
            btAdapter?.state?.let {state ->
                bluetoothStateBroadcastReceiver.onReceive(appContext, Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
                    putExtra(BluetoothAdapter.EXTRA_STATE, state)
                })
            }
        }
    }

    override suspend fun onNewNodeDiscovered(node: NetworkNode) {
        Napier.d("Found device: ${node.bluetoothMacAddress} / ${node.nodeName}", tag = LOGTAG)
        node.nodeId = db.networkNodeDao.insertAsync(node)
        networkNodesMap[node.nodeId] = node
        networkNodesLiveDataInternal.sendValue(networkNodesMap.values.toList())
    }

    suspend fun findByBluetoothAddr(bluetoothAddr: String) : NetworkNode? {
        return networkNodesMap.values.firstOrNull { it.bluetoothMacAddress == bluetoothAddr }
    }

    suspend fun onNodeUpdated(node: NetworkNode) {
        networkNodesMap[node.nodeId] = node
        db.networkNodeDao.update(node)
        networkNodesLiveDataInternal.sendValue(networkNodesMap.values.toList())
    }

    override suspend fun handleNodesLost(bluetoothAddrs: List<String>) {

    }

    override fun addMonitoringRequest(request: AvailabilityMonitorRequest) {

    }

    override fun removeMonitoringRequest(request: AvailabilityMonitorRequest) {

    }

    override suspend fun areContentEntriesLocallyAvailable(containerUids: List<Long>): Map<Long, Boolean> {
        return mapOf()
    }

    override suspend fun findBestLocalNodeForContentEntryDownload(containerUid: Long): NetworkNode? {
        return null
    }

    companion object {
        const val LOGTAG = "LocalAvailibilityManagerAndroid"
    }

}