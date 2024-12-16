package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.NeighborCache


@DoorDao
expect abstract class NeighborCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(neighborCache: NeighborCache)

    @Query("""
        DELETE FROM NeighborCache
         WHERE neighborUid = :neighborUid
    """)
    abstract suspend fun deleteAsync(neighborUid: Long)

}