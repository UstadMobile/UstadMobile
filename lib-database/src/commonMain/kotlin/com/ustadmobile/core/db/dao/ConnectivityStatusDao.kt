package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.door.annotation.DoorDao
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.lib.db.entities.ConnectivityStatus

@DoorDao
expect abstract class ConnectivityStatusDao {

    @Query("SELECT ConnectivityStatus.* FROM ConnectivityStatus LIMIT 1")
    abstract fun statusLive(): Flow<ConnectivityStatus?>

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

}
