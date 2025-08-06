package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.composites.NeighborCacheEntryAndNeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCache
import com.ustadmobile.libcache.db.entities.NeighborCacheEntry
import kotlinx.coroutines.flow.Flow

@DoorDao
expect abstract class NeighborCacheEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsertList(neighborCacheEntryList: List<NeighborCacheEntry>)

    @Query("""
        SELECT NeighborCacheEntry.* 
          FROM NeighborCacheEntry
    """)
    abstract fun allEntriesAsFlow(): Flow<List<NeighborCacheEntry>>


    @Query("""
        SELECT NeighborCacheEntry.nceUrlHash
          FROM NeighborCacheEntry
         WHERE NeighborCacheEntry.nceUrlHash IN (:urlHashes) 
    """)
    abstract fun findAvailableEntries(
        urlHashes: List<Long>
    ): List<Long>

    @Query("""
        SELECT NeighborCacheEntry.*,
               NeighborCache.*
          FROM NeighborCacheEntry
               JOIN NeighborCache
                    ON NeighborCache.neighborUid = NeighborCacheEntry.nceNeighborUid
         WHERE NeighborCacheEntry.nceUrlHash = :urlHash
           AND NeighborCache.neighborStatus = ${NeighborCache.STATUS_ACTIVE}
    """)
    abstract fun findAvailableNeighborsByUrlHash(
        urlHash: Long
    ): List<NeighborCacheEntryAndNeighborCache>

}