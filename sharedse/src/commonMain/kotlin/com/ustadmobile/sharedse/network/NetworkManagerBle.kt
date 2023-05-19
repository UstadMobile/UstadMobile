package com.ustadmobile.sharedse.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.kodein.di.DI

expect open class NetworkManagerBle(
    context: Any = Any(),
    di: DI,
    singleThreadDispatcher: CoroutineDispatcher = Dispatchers.Default
) : NetworkManagerBleCommon {

    override val isWiFiEnabled: Boolean

    override val isBluetoothEnabled: Boolean

    override fun openBluetoothSettings()

}