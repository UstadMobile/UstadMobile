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


    @Query("""
        DELETE FROM NeighborCache
         WHERE neighborUid = :neighborUid
    """)
    abstract suspend fun deleteAsync(neighborUid: Long)

    @Query("""
        SELECT NeighborCache.*
          FROM NeighborCache
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


}