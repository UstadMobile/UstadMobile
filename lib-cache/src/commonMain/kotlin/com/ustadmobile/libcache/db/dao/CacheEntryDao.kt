package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.CacheEntry

@DoorDao
expect abstract class CacheEntryDao {

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.url = :url 
    """)
    abstract suspend fun findByUrlAsync(url: String): CacheEntry?

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.key = :key
    """)
    abstract fun findEntryAndBodyByKey(
        key: String,
    ): CacheEntry?

    @Insert
    abstract suspend fun insertAsync(entry: CacheEntry): Long

    @Insert
    abstract fun insertList(entry: List<CacheEntry>)

    @Update
    abstract fun updateList(entry: List<CacheEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsertList(entry: List<CacheEntry>)

    @Query(
        """
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.key IN
               (SELECT RequestedEntry.requestedKey
                  FROM RequestedEntry
                 WHERE RequestedEntry.batchId = :batchId)
    """
    )
    abstract fun findByRequestBatchId(batchId: Int): List<CacheEntry>

    @Query("""
        UPDATE CacheEntry
           SET lastAccessed = :lastAccessTime
         WHERE key = :key  
    """)
    abstract fun updateLastAccessedTime(key: String, lastAccessTime: Long)


}