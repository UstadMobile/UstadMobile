package com.ustadmobile.libcache.db.dao

import androidx.room.Delete
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
        SELECT RequestedEntry.requestedKey
          FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId
           AND EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = RequestedEntry.requestedKey)
    """)
    abstract fun findEntriesWithLock(batchId: Int): List<String>




    @Query("""
        UPDATE CacheEntry
           SET lastAccessed = :lastAccessTime
         WHERE key = :key  
    """)
    abstract fun updateLastAccessedTime(key: String, lastAccessTime: Long)

    /**
     * Find entries that can be evicted e.g. entries for which there is no RetentionLock
     */
    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE NOT EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = CacheEntry.key) 
      ORDER BY lastAccessed ASC           
         LIMIT :batchSize       
      
    """)
    abstract fun findEvictableEntries(batchSize: Int): List<CacheEntry>

    /**
     * Get the total size of evictable entries.
     */
    @Query("""
        SELECT SUM(CacheEntry.storageSize)
          FROM CacheEntry
         WHERE NOT EXISTS(
               SELECT RetentionLock.lockId
                 FROM RetentionLock
                WHERE RetentionLock.lockKey = CacheEntry.key)  
    """)
    abstract fun totalEvictableSize(): Long

    @Delete
    abstract fun delete(entries: List<CacheEntry>)

    @Query("""
        UPDATE CacheEntry
           SET responseHeaders = :headers,
               lastValidated = :lastValidated,
               lastAccessed = :lastAccessed
         WHERE key = :key      
    """)
    abstract fun updateValidation(
        key: String,
        headers: String,
        lastValidated: Long,
        lastAccessed: Long,
    )

}