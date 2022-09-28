package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.StateContentEntity
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class StateContentDao : BaseDao<StateContentEntity> {

    @Query("""
     REPLACE INTO StateContentEntityReplicate(scePk, sceDestination)
      SELECT DISTINCT StateContentEntity.stateContentUid AS scePk,
             :newNodeId AS sceDestination
        FROM StateContentEntity
       WHERE StateContentEntity.stateContentLct != COALESCE(
             (SELECT sceVersionId
                FROM StateContentEntityReplicate
               WHERE scePk = StateContentEntity.stateContentUid
                 AND sceDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(scePk, sceDestination) DO UPDATE
             SET scePending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([StateContentEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO StateContentEntityReplicate(scePk, sceDestination)
  SELECT DISTINCT StateContentEntity.stateContentUid AS sceUid,
         UserSession.usClientNodeId AS sceDestination
    FROM ChangeLog
         JOIN StateContentEntity
             ON ChangeLog.chTableId = ${StateContentEntity.TABLE_ID}
                AND ChangeLog.chEntityPk = StateContentEntity.stateContentUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND StateContentEntity.stateContentLct != COALESCE(
         (SELECT sceVersionId
            FROM StateContentEntityReplicate
           WHERE scePk = StateContentEntity.stateContentUid
             AND sceDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(scePk, sceDestination) DO UPDATE
     SET scePending = true
  */               
 """)
    @ReplicationRunOnChange([StateContentEntity::class])
    @ReplicationCheckPendingNotificationsFor([StateContentEntity::class])
    abstract suspend fun replicateOnChange()

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :id AND isIsactive")
    abstract fun findAllStateContentWithStateUid(id: Long): List<StateContentEntity>

    @Query("SELECT * FROM StateContentEntity WHERE stateContentStateUid = :stateUid AND stateContentKey = :key AND isIsactive")
    abstract fun findStateContentByKeyAndStateUid(key: String, stateUid: Long): StateContentEntity?

    @Query("""
        UPDATE StateContentEntity 
           SET isIsactive = :isActive,  
               stateContentLct = :updateTime
         WHERE stateContentUid = :stateUid
    """)
    abstract fun setInActiveStateContentByKeyAndUid(
        isActive: Boolean,
        stateUid: Long,
        updateTime: Long,
    )


}
