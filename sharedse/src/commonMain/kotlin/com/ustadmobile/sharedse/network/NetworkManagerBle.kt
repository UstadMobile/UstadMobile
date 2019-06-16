package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.NetworkNode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

expect class NetworkManagerBle(context: Any = Any(),
                               singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default) : NetworkManagerBleCommon {

    override val isWiFiEnabled: Boolean

    override val isBleCapable: Boolean

    override val isBluetoothEnabled: Boolean

    override val isVersionLollipopOrAbove: Boolean

    override val isVersionKitKatOrBelow: Boolean

    override fun canDeviceAdvertise(): Boolean

    override fun openBluetoothSettings()

    override fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle

    override fun setWifiEnabled(enabled: Boolean): Boolean

    override fun connectToWiFi(ssid: String, passphrase: String, timeout: Int)

    override fun restoreWifi()

    override fun makeEntryStatusTask(context: Any?, entryUidsToCheck: List<Long>, peerToCheck: NetworkNode?): BleEntryStatusTask?

    override fun makeEntryStatusTask(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode, responseListener: BleMessageResponseListener): BleEntryStatusTask?

    override fun makeDeleteJobTask(`object`: Any?, args: Map<String, String>): DeleteJobTaskRunner
}