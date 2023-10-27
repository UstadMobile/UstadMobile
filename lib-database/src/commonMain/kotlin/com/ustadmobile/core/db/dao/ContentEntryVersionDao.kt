package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryVersion

@DoorDao
@Repository
expect abstract class ContentEntryVersionDao {

    @Query("""
        SELECT ContentEntryVersion.*
          FROM ContentEntryVersion
         WHERE cevUid = :cevUid 
    """)
    abstract suspend fun findByUidAsync(cevUid: Long): ContentEntryVersion?

    @Insert
    abstract suspend fun insertAsync(contentEntryVersion: ContentEntryVersion): Long

    @Query("""
        SELECT ContentEntryVersion.*
          FROM ContentEntryVersion
         WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid
      ORDER BY ContentEntryVersion.cevLastModified DESC
         LIMIT 1
    """)
    abstract suspend fun findLatestVersionUidByContentEntryUidEntity(
        contentEntryUid: Long
    ): ContentEntryVersion?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findLatestVersionUidByContentEntryUidEntity")
        )
    )
    @Query("""
        SELECT COALESCE(
               (SELECT ContentEntryVersion.cevUid
                  FROM ContentEntryVersion
                 WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid
              ORDER BY ContentEntryVersion.cevLastModified DESC
                 LIMIT 1), 0) 
    """)
    abstract suspend fun findLatestVersionUidByContentEntryUid(contentEntryUid: Long): Long

}