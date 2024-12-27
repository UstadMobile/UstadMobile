package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.NeighborCache
import kotlinx.coroutines.flow.Flow


@DoorDao
expect abstract class NeighborCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(neighborCache: NeighborCache)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsert(neighborCache: NeighborCache)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertOrIgnore(neighborCache: NeighborCache)

    @Query("""
        DELETE FROM NeighborCache
         WHERE neighborUid = :neighborUid
    """)
    abstract suspend fun deleteAsync(neighborUid: Long)

    @Query("""
        SELECT NeighborCache.*
          FROM NeighborCache
         WHERE NeighborCache.neighborStatus = ${NeighborCache.STATUS_ACTIVE} 
    """)
    abstract fun allNeighborsAsFlow(): Flow<List<NeighborCache>>

    @Query("""
        SELECT NeighborCache.*
          FROM NeighborCache
    """)
    abstract fun allNeighbors(): List<NeighborCache>

    @Query("""
        UPDATE NeighborCache
           SET neighborHttpPort = :httpPort
         WHERE neighborUid = :neighborUid
           AND neighborHttpPort != :httpPort
    """)
    abstract fun updateHttpPort(neighborUid: Long, httpPort: Int)

    @Query("""
        UPDATE NeighborCache
           SET neighborPingTime = :pingTime,
               neighborLastSeen = :timeNow
         WHERE neighborUid = :neighborUid  
    """)
    abstract fun updatePingTime(neighborUid: Long, pingTime: Int, timeNow: Long): Int

    /**
     * Update the neighbor status based on whether or not it has replied to a ping within the threshold
     * time
     *
     * @param timeNow current system time
     * @param lostThreshold the threshold (in ms) after which, if a node has not replied to a ping, it is considered lost
     */
    @Query("""
        UPDATE NeighborCache
           SET neighborStatus = CAST(((:timeNow - NeighborCache.neighborLastSeen) < :lostThreshold) AS INTEGER)
         WHERE neighborStatus != CAST(((:timeNow - NeighborCache.neighborLastSeen) < :lostThreshold) AS INTEGER)   
    """)
    abstract fun updateStatuses(timeNow: Long, lostThreshold: Long)

    @Query("""
        UPDATE NeighborCache
           SET neighborDeviceName = :deviceName
        WHERE neighborUid = :neighborUid
          AND neighborDeviceName != :deviceName   
    """)
    abstract fun updateDeviceName(neighborUid: Long, deviceName: String)


}