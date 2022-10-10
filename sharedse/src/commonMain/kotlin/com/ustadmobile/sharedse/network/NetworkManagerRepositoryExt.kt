package com.ustadmobile.sharedse.network

import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_METERED
import com.ustadmobile.lib.db.entities.ConnectivityStatus.Companion.STATE_UNMETERED
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


inline fun <reified T: DoorDatabaseRepository> T.setupWithNetworkManager(networkManagerBle: NetworkManagerBle): T {
    GlobalScope.launch(doorMainDispatcher()) {
        networkManagerBle.connectivityStatus.observeForever(object : Observer<ConnectivityStatus> {
            override fun onChanged(t: ConnectivityStatus) {
                this@setupWithNetworkManager.connectivityStatus = if(t.connectivityState == STATE_UNMETERED || t.connectivityState == STATE_METERED) {
                    DoorDatabaseRepository.STATUS_CONNECTED
                }else {
                    DoorDatabaseRepository.STATUS_DISCONNECTED
                }
            }
        })
    }

    return this
}
