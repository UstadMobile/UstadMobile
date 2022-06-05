package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentEntryPicture
import com.ustadmobile.lib.db.entities.UserSession


@Dao
@Repository
abstract class ContentEntryPictureDao : BaseDao<ContentEntryPicture> {

    @Query("""
     REPLACE INTO ContentEntryPictureReplicate(cepPk, cepDestination)
         SELECT DISTINCT cepUid AS cepPK,
                :newNodeId AS siteDestination
           FROM ContentEntryPicture
          WHERE ContentEntryPicture.cepTimestamp != COALESCE(
                (SELECT cepVersionId
                   FROM ContentEntryPictureReplicate
                  WHERE cepPk = ContentEntryPicture.cepUid
                    AND cepDestination = :newNodeId), -1) 
         /*psql ON CONFLICT(cepPk, cepDestination) DO UPDATE
                SET cepPending = true
         */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentEntryPicture::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)


    @Query("""
  REPLACE INTO ContentEntryPictureReplicate(cepPk, cepDestination)
         SELECT DISTINCT ContentEntryPicture.cepUid AS cepPk,
                UserSession.usClientNodeId AS siteDestination
           FROM ChangeLog
                JOIN ContentEntryPicture
                    ON ChangeLog.chTableId = ${ContentEntryPicture.TABLE_ID}
                       AND ChangeLog.chEntityPk = ContentEntryPicture.cepUid
                JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
          WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
            AND ContentEntryPicture.cepTimestamp != COALESCE(
                (SELECT cepVersionId
                   FROM ContentEntryPictureReplicate
                  WHERE cepPk = ContentEntryPicture.cepUid
                    AND cepDestination = UserSession.usClientNodeId), 0)     
        /*psql ON CONFLICT(cepPk, cepDestination) DO UPDATE
            SET cepPending = true
         */               
    """)
    @ReplicationRunOnChange([ContentEntryPicture::class])
    @ReplicationCheckPendingNotificationsFor([ContentEntryPicture::class])
    abstract suspend fun replicateOnChange()

    @Query("""
        SELECT * 
          FROM ContentEntryPicture 
         WHERE cepContentEntryUid = :entryUid
           AND cepActive
      ORDER BY cepTimestamp DESC 
         LIMIT 1
         """)
    abstract suspend fun findByContentEntryUidAsync(entryUid: Long): ContentEntryPicture?

    @Query("""
         SELECT * 
          FROM ContentEntryPicture 
         WHERE cepContentEntryUid = :entryUid
           AND cepActive
      ORDER BY cepTimestamp DESC 
         LIMIT 1
         """)
    abstract fun findByContentEntryUidLive(entryUid: Long): DoorLiveData<ContentEntryPicture?>

    @Update
    abstract suspend fun updateAsync(ContentEntryPicture: ContentEntryPicture)


}