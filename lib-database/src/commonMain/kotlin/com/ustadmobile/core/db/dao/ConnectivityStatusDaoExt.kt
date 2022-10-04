package com.ustadmobile.core.db.dao

import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.lifecycle.Observer
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun ConnectivityStatusDao.commitLiveConnectivityStatus(connectivityStatusLive: LiveData<ConnectivityStatus>) {
    val conenctivityStatusObserver = Observer<ConnectivityStatus> { t ->
        GlobalScope.launch {
            insertAsync(t)
        }
    }

    GlobalScope.launch(doorMainDispatcher()) {
        connectivityStatusLive.observeForever(conenctivityStatusObserver)
    }
}