package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.DoorObserver
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ConnectivityStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Dao
abstract class ConnectivityStatusDao {

    val conenctivityStatusObserver = object : DoorObserver<ConnectivityStatus> {
        override fun onChanged(t: ConnectivityStatus) {
            GlobalScope.launch {
                insertAsync(t)
            }
        }
    }

    @Query("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    abstract fun statusLive(): DoorLiveData<ConnectivityStatus?>

    @Query("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    abstract fun status(): ConnectivityStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(connectivityStatus: ConnectivityStatus): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAsync(connectivityStatus: ConnectivityStatus): Long

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState")
    abstract suspend fun updateStateAsync(connectivityState: Int)

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    abstract suspend fun updateState(connectivityState: Int, wifiSsid: String)

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    abstract fun updateStateSync(connectivityState: Int, wifiSsid: String)

    @Transaction
    open suspend fun addConnectivityStatusRecord(state: Int, wifiSsid: String, connectedOrConnecting: Boolean) {
        val connectivityStatus = ConnectivityStatus()
        connectivityStatus.connectedOrConnecting = connectedOrConnecting
        connectivityStatus.connectivityState = state
        connectivityStatus.wifiSsid = wifiSsid
        insertAsync(connectivityStatus)
    }

    fun commitLiveConnectivityStatus(connectivityStatusLive: DoorLiveData<ConnectivityStatus>) {
        GlobalScope.launch(doorMainDispatcher()) {
            connectivityStatusLive.observeForever(conenctivityStatusObserver)
        }
    }

}
