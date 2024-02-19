package com.ustadmobile.libcache.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.RetentionLock

@DoorDao
expect abstract class RetentionLockDao {

    @Insert
    abstract fun insert(retentionLock: RetentionLock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun upsertList(retentionLocks: List<RetentionLock>)

    @Delete
    abstract fun delete(retentionLocks: List<RetentionLock>)

    @Query("""
        SELECT RetentionLock.*
          FROM RetentionLock
         WHERE RetentionLock.lockKey IN 
               (SELECT RequestedEntry.requestedKey
                  FROM RequestedEntry
                 WHERE RequestedEntry.batchId = :batchId)
    """)
    abstract fun findByBatchId(batchId: Int): List<RetentionLock>

    @Query("""
        SELECT RetentionLock.*
          FROM RetentionLock
         WHERE RetentionLock.lockKey = :urlKey 
    """)
    abstract fun findByKey(urlKey: String): List<RetentionLock>

}