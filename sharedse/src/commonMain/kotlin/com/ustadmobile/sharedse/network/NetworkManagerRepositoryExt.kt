package com.ustadmobile.sharedse.network

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED


inline fun <reified T: DoorDatabaseRepository> T.setupWithNetworkManager(networkManagerBle: NetworkManagerBle): T {
    networkManagerBle.connectivityStatus.observeForever(object : DoorObserver<ConnectivityStatus> {
        override fun onChanged(t: ConnectivityStatus) {
            this@setupWithNetworkManager.connectivityStatus = if(t.connectivityState == STATE_UNMETERED || t.connectivityState == STATE_METERED) {
                DoorDatabaseRepository.STATUS_CONNECTED
            }else {
                DoorDatabaseRepository.STATUS_DISCONNECTED
            }
        }
    })

    return this
}
