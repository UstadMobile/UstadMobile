package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
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

}