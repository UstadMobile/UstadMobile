package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.flow.Flow

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

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
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
    )
    @Query("""
        SELECT ContentEntryVersion.*
          FROM ContentEntryVersion
         WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid
      ORDER BY ContentEntryVersion.cevLastModified DESC
         LIMIT 1
    """)
    abstract fun findLatestByContentEntryUidAsFlow(
        contentEntryUid: Long
    ): Flow<ContentEntryVersion?>


    @Query("""
        UPDATE TransferJobItem
           SET tjiEntityEtag = 
               (SELECT cevLct
                  FROM ContentEntryVersion
                 WHERE cevUid = :entityUid)
         WHERE tjiUid = :transferJobItemUid
    """)
    abstract suspend fun updateTransferJobItemEtag(
        entityUid: Long,
        transferJobItemUid: Int
    )


    @Query("""
        SELECT ContentEntryVersion.*
          FROM ContentEntryVersion
         WHERE NOT EXISTS(
               SELECT CacheLockJoin.cljId
                 FROM CacheLockJoin
                WHERE CacheLockJoin.cljTableId = ${ContentEntryVersion.TABLE_ID}
                  AND CacheLockJoin.cljEntityUid = ContentEntryVersion.cevUid
                  AND CacheLockJoin.cljUrl = ContentEntryVersion.cevManifestUrl) 
    """)
    abstract suspend fun findContentEntryVersionsWithoutCacheLock(): List<ContentEntryVersion>

}