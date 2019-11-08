package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.NetworkNode
import kotlinx.coroutines.CoroutineDispatcher

actual open class NetworkManagerBle actual constructor(context: Any, singleThreadDispatcher: CoroutineDispatcher) : NetworkManagerBleCommon() {
    actual override val isWiFiEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual override val isBleCapable: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual override val isBluetoothEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual override val isVersionLollipopOrAbove: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual override val isVersionKitKatOrBelow: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    actual override fun canDeviceAdvertise(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun openBluetoothSettings() {
    }

    actual override fun setWifiEnabled(enabled: Boolean): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun connectToWiFi(ssid: String, passphrase: String, timeout: Int) {
    }

    actual override fun restoreWifi() {
    }

    actual override suspend fun makeEntryStatusTask(context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode): BleEntryStatusTask? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun makeEntryStatusTask(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode, responseListener: BleMessageResponseListener): BleEntryStatusTask? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}