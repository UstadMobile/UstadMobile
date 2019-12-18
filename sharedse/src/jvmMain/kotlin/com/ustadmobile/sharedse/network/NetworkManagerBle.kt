package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.NetworkNode
import com.ustadmobile.sharedse.network.containerfetcher.ConnectionOpener
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcher
import com.ustadmobile.sharedse.network.containerfetcher.ContainerFetcherBuilder
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.OkHttpClient

actual open class NetworkManagerBle actual constructor(context: Any, singleThreadDispatcher: CoroutineDispatcher,
                                                       umAppDatabase: UmAppDatabase) : NetworkManagerBleCommon(umAppDatabase),
        NetworkManagerWithConnectionOpener{


    override val containerDownloadManager: ContainerDownloadManager
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

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

    override val localHttpPort: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val containerFetcher: ContainerFetcher by lazy {
        ContainerFetcherBuilder(this).build()
    }

    override val localConnectionOpener: ConnectionOpener?
        get() = null

    override val umAppDatabaseRepo by lazy {
        val activeAccount = UmAccountManager.getActiveAccount(context)
        val serverUrl = if(activeAccount!= null) {
            activeAccount.endpointUrl ?: "http://localhost"
        }else {
            UstadMobileSystemImpl.instance.getAppConfigString("apiUrl",
                    "http://localhost", context) ?: "http://localhost"
        }
        umAppDatabase.asRepository<UmAppDatabase>(context, serverUrl, "", defaultHttpClient(),
                null)
    }

    override suspend fun sendBleMessage(context: Any, bleMessage: BleMessage, deviceAddr: String): BleMessage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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