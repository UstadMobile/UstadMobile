package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzContentJoin
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@Repository
@DoorDao
expect abstract class ClazzContentJoinDao: BaseDao<ClazzContentJoin> {


    @Query("""
     REPLACE INTO ClazzContentJoinReplicate(ccjPk, ccjDestination)
      SELECT DISTINCT ClazzContentJoin.ccjUid AS ccjUid,
             :newNodeId AS ccjDestination
        FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_CLAZZ_CONTENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzContentJoin    
                    ON Clazz.clazzUid = ClazzContentJoin.ccjClazzUid
       WHERE UserSession.usClientNodeId = :newNodeId 
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}             
         AND ClazzContentJoin.ccjLct != COALESCE(
             (SELECT ccjVersionId
                FROM ClazzContentJoinReplicate
               WHERE ccjPk = ClazzContentJoin.ccjUid
                 AND ccjDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(ccjPk, ccjDestination) DO UPDATE
             SET ccjPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzContentJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ClazzContentJoinReplicate(ccjPk, ccjDestination)
  SELECT DISTINCT ClazzContentJoin.ccjUid AS ccjUid,
         UserSession.usClientNodeId AS ccjDestination
    FROM ChangeLog
         JOIN ClazzContentJoin
             ON ChangeLog.chTableId = ${ClazzContentJoin.TABLE_ID}
                AND ChangeLog.chEntityPk = ClazzContentJoin.ccjUid
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzContentJoin.ccjClazzUid                
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_CLAZZ_CONTENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzContentJoin.ccjLct != COALESCE(
         (SELECT ccjVersionId
            FROM ClazzContentJoinReplicate
           WHERE ccjPk = ClazzContentJoin.ccjUid
             AND ccjDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(ccjPk, ccjDestination) DO UPDATE
     SET ccjPending = true
  */               
    """)
    @ReplicationRunOnChange([ClazzContentJoin::class])
    @ReplicationCheckPendingNotificationsFor([ClazzContentJoin::class])
    abstract suspend fun replicateOnChange()

    @Query("""UPDATE ClazzContentJoin 
                       SET ccjActive = :toggleVisibility, 
                           ccjLct = :changedTime 
                     WHERE ccjContentEntryUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityClazzContent(
        toggleVisibility: Boolean,
        selectedItem: List<Long>,
        changedTime: Long
    )

    @Query("""
        SELECT ccjContentEntryUid 
          FROM ClazzContentJoin
         WHERE ccjClazzUid = :clazzUid
           AND ccjActive
    """)
    abstract suspend fun listOfEntriesInClazz(clazzUid: Long): List<Long>

}