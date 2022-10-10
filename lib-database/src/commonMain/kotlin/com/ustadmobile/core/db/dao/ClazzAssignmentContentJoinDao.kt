package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.core.db.dao.ClazzAssignmentContentJoinDaoCommon.FINDBY_CLAZZ_ASSIGNMENT_UID
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ClazzAssignmentContentJoinDao : BaseDao<ClazzAssignmentContentJoin>{

    @Query("""
     REPLACE INTO ClazzAssignmentContentJoinReplicate(cacjPk, cacjDestination)
      SELECT DISTINCT ClazzAssignmentContentJoin.cacjUid AS cacjUid,
             :newNodeId AS cacjDestination
        FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2} 
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid
               JOIN ClazzAssignmentContentJoin
                    ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid     
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}             
         AND ClazzAssignmentContentJoin.cacjLct != COALESCE(
             (SELECT cacjVersionId
                FROM ClazzAssignmentContentJoinReplicate
               WHERE cacjPk = ClazzAssignmentContentJoin.cacjUid
                 AND cacjDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(cacjPk, cacjDestination) DO UPDATE
             SET cacjPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzAssignmentContentJoin::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ClazzAssignmentContentJoinReplicate(cacjPk, cacjDestination)
  SELECT DISTINCT ClazzAssignmentContentJoin.cacjUid AS cacjUid,
         UserSession.usClientNodeId AS cacjDestination
    FROM ChangeLog
         JOIN ClazzAssignmentContentJoin
             ON ChangeLog.chTableId = ${ClazzAssignmentContentJoin.TABLE_ID}
                AND ChangeLog.chEntityPk = ClazzAssignmentContentJoin.cacjUid
         JOIN ClazzAssignment
              ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzAssignmentContentJoin.cacjLct != COALESCE(
         (SELECT cacjVersionId
            FROM ClazzAssignmentContentJoinReplicate
           WHERE cacjPk = ClazzAssignmentContentJoin.cacjUid
             AND cacjDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(cacjPk, cacjDestination) DO UPDATE
     SET cacjPending = true
  */               
 """)
    @ReplicationRunOnChange([ClazzAssignmentContentJoin::class])
 @ReplicationCheckPendingNotificationsFor([ClazzAssignmentContentJoin::class])
 abstract suspend fun replicateOnChange()

    @Query(FINDBY_CLAZZ_ASSIGNMENT_UID)
    abstract suspend fun findAllContentByClazzAssignmentUidAsync(clazzAssignmentUid: Long, personUid : Long)
            :List <ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>

    @Query(FINDBY_CLAZZ_ASSIGNMENT_UID)
    abstract fun findAllContentByClazzAssignmentUidDF(clazzAssignmentUid: Long, personUid : Long)
            : DataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>



}
