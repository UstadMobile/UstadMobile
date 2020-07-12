package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI

expect open class NetworkManagerBle(context: Any = Any(),
                                    di: DI,
                                    singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default)
    : NetworkManagerBleCommon {

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

    override suspend fun makeEntryStatusTask(context: Any, containerUidsToCheck: List<Long>, networkNode: NetworkNode): BleEntryStatusTask?

    override fun makeEntryStatusTask(context: Any, message: BleMessage, peerToSendMessageTo: NetworkNode, responseListener: BleMessageResponseListener): BleEntryStatusTask?

}