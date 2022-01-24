package com.ustadmobile.core.networkmanager

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus

/**
 * This wrapper just makes things easier for retrieval via the DI by creating a dedicated class
 * type.
 *
 * This is kept in the DI to avoid multiple classes creating their own copy.
 */
class ConnectivityLiveData(
        val liveData: DoorLiveData<ConnectivityStatus?>
)
