package com.ustadmobile.sharedse.network

import com.ustadmobile.sharedse.network.containerfetcher.ConnectionOpener
import kotlinx.coroutines.CoroutineDispatcher
import org.kodein.di.DI

actual open class NetworkManagerBle actual constructor(context: Any, di: DI, singleThreadDispatcher: CoroutineDispatcher) : NetworkManagerBleCommon(context, di,singleThreadDispatcher),
        NetworkManagerWithConnectionOpener{



    actual override val isWiFiEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    actual override val isBluetoothEnabled: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val localHttpPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


    override val localConnectionOpener: ConnectionOpener?
        get() = null

    actual override fun openBluetoothSettings() {
    }

    actual override fun setWifiEnabled(enabled: Boolean): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual override fun connectToWiFi(ssid: String, passphrase: String, timeout: Int) {
    }

    actual override fun restoreWifi() {
    }

    actual override fun awaitWifiDirectGroupReady(timeout: Long): WiFiDirectGroupBle {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



}