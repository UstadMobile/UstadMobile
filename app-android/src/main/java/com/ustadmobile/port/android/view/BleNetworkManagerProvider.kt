package com.ustadmobile.port.android.view

import kotlinx.coroutines.CompletableDeferred
import com.ustadmobile.sharedse.network.NetworkManagerBle

/**
 * Common interface used to provide NetworkManagerBle
 * @author kileha3
 */
interface BleNetworkManagerProvider {
    var networkManager: CompletableDeferred<NetworkManagerBle>?
}