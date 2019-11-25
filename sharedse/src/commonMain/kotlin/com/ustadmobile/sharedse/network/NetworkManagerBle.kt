package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.fetch.FetchMpp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect open class NetworkManagerBle(context: Any = Any(),
                                    singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default,
                                    umAppDatabase: UmAppDatabase = UmAppDatabase.getInstance(context))
    : NetworkManagerBleCommon {

    override val isWiFiEnabled: Boolean

    override val isBleCapable: Boolean

    override val isBluetoothEnabled: Boolean

    override val isVersionLollipopOrAbove: Boolean

    override val isVersionKitKatOrBelow: Boolean

    override val httpFetcher: FetchMpp

    override fun canDeviceAdvertise(): Boolean

    override fun openBluetoothSettings()

    override fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle

    override fun setWifiEnabled(enabled: Boolean): Boolean

    override fun connectToWiFi(ssid: String, passphrase: String, timeout: Int)

    override fun restoreWifi()

    override suspend fun makeEntryStatusTask(context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode): BleEntryStatusTask?

    override fun makeEntryStatusTask(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode, responseListener: BleMessageResponseListener): BleEntryStatusTask?

}