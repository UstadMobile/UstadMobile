package com.ustadmobile.libcache.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.libcache.db.entities.CacheEntry
import com.ustadmobile.libcache.db.entities.CacheEntryAndBody

@DoorDao
expect abstract class CacheEntryDao {

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.url = :url 
    """)
    abstract suspend fun findByUrlAsync(url: String): CacheEntry?

    @Query("""
        SELECT CacheEntry.*, ResponseBody.*
          FROM CacheEntry
               LEFT JOIN ResponseBody
                    ON ResponseBody.responseId = 
                       (SELECT ResponseBody.responseId
                          FROM ResponseBody
                         WHERE ResponseBody.sha256 = CacheEntry.responseBodySha256
                         LIMIT 1)
         WHERE CacheEntry.url = :url
            OR ((:withoutSearchParams IS NOT NULL) 
                AND CacheEntry.url = :withoutSearchParams
                AND (CacheEntry.cacheFlags & ${CacheEntry.CACHE_FLAG_STATIC}) = ${CacheEntry.CACHE_FLAG_STATIC})
      ORDER BY (CASE
                WHEN CacheEntry.url = :url THEN 0
                ELSE 1
                END)
         LIMIT 1
    """)
    abstract fun findEntryAndBodyByUrl(
        url: String,
        withoutSearchParams: String?,
    ): CacheEntryAndBody?

    @Insert
    abstract suspend fun insertAsync(entry: CacheEntry): Long

    @Insert
    abstract fun insertList(entry: List<CacheEntry>)

    @Query("""
        SELECT CacheEntry.*
          FROM CacheEntry
         WHERE CacheEntry.url IN
               (SELECT RequestedEntry.requestedUrl
                  FROM RequestedEntry
                 WHERE RequestedEntry.batchId = :batchId)
    """)
    abstract fun findByRequestBatchId(batchId: Int): List<CacheEntry>


}