package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.RequestedEntry

@DoorDao
expect abstract class RequestedEntryDao {

    @Insert
    abstract fun insertList(requestedSha256s: List<RequestedEntry>)

    @Insert
    abstract suspend fun insertListAsync(requestedSha256s: List<RequestedEntry>)

    @Query(
        """
        SELECT RequestedEntry.requestSha256
          FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId
           AND NOT EXISTS(
               SELECT ResponseBody.responseId
                 FROM ResponseBody
                WHERE ResponseBody.sha256 = RequestedEntry.requestSha256
                LIMIT 1)
    """
    )
    abstract fun findSha256sNotPresent(batchId: Int): List<String>

    @Query(
        """
        SELECT RequestedEntry.requestedUrl
          FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId 
           AND NOT EXISTS(
               SELECT CacheEntry.ceId
                 FROM CacheEntry
                WHERE CacheEntry.url = RequestedEntry.requestedUrl
           )
        """
    )
    abstract fun findUrlsNotPresent(
        batchId: Int,
    ): List<String>

    @Query("""
        DELETE FROM RequestedEntry
         WHERE RequestedEntry.batchId = :batchId    
    """)
    abstract fun deleteBatch(batchId: Int)

}