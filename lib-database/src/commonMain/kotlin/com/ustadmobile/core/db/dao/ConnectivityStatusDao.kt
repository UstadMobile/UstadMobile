package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.lib.db.entities.ConnectivityStatus

@Dao
abstract class ConnectivityStatusDao {

    @Query("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    abstract fun statusLive(): UmLiveData<ConnectivityStatus>

    @Query("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    abstract fun status(): ConnectivityStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(connectivityStatus: ConnectivityStatus): Long

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState")
    abstract suspend fun updateStateAsync(connectivityState: Int)

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    abstract suspend fun updateState(connectivityState: Int, wifiSsid: String)

    @Query("UPDATE ConnectivityStatus SET connectivityState = :connectivityState , wifiSsid = :wifiSsid")
    abstract fun updateStateSync(connectivityState: Int, wifiSsid: String)

    suspend fun addConnectivityStatusRecord(state: Int, wifiSsid: String, connectedOrConnecting: Boolean) {
        val connectivityStatus = ConnectivityStatus()
        connectivityStatus.connectedOrConnecting = connectedOrConnecting
        connectivityStatus.connectivityState = state
        connectivityStatus.wifiSsid = wifiSsid
        insert(connectivityStatus)
    }
}
