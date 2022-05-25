package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.PeerReviewerAllocation
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class PeerReviewerAllocationDao : BaseDao<PeerReviewerAllocation>, OneToManyJoinDao<PeerReviewerAllocation>{

    @Query("""
     REPLACE INTO PeerReviewerAllocationReplicate(prarPk, prarDestination)
      SELECT DISTINCT PeerReviewerAllocation.praUid AS praUid,
             :newNodeId AS prarDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ClazzAssignment
                  ON ClazzAssignment.caClazzUid = Clazz.clazzUid
             JOIN PeerReviewerAllocation
                    ON PeerReviewerAllocation.praAssignmentUid = ClazzAssignment.caUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND PeerReviewerAllocation.praLct != COALESCE(
             (SELECT prarVersionId
                FROM PeerReviewerAllocationReplicate
               WHERE prarPk = PeerReviewerAllocation.praUid
                 AND prarDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(prarPk, prarDestination) DO UPDATE
             SET prarPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([PeerReviewerAllocation::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO PeerReviewerAllocationReplicate(prarPk, prarDestination)
  SELECT DISTINCT PeerReviewerAllocation.praUid AS prarUid,
         UserSession.usClientNodeId AS prarDestination
    FROM ChangeLog
         JOIN PeerReviewerAllocation
             ON ChangeLog.chTableId = ${PeerReviewerAllocation.TABLE_ID}
                AND ChangeLog.chEntityPk = PeerReviewerAllocation.praUid
         JOIN ClazzAssignment
              ON PeerReviewerAllocation.praAssignmentUid = ClazzAssignment.caUid       
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND PeerReviewerAllocation.praLct != COALESCE(
         (SELECT prarVersionId
            FROM PeerReviewerAllocationReplicate
           WHERE prarPk = PeerReviewerAllocation.praUid
             AND prarDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(prarPk, prarDestination) DO UPDATE
     SET prarPending = true
  */               
 """)
    @ReplicationRunOnChange([PeerReviewerAllocation::class])
    @ReplicationCheckPendingNotificationsFor([PeerReviewerAllocation::class])
    abstract suspend fun replicateOnChange()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun replaceListAsync(entries: List<PeerReviewerAllocation>)

    @Query("""
        SELECT *
         FROM PeerReviewerAllocation
        WHERE praAssignmentUid IN (:assignmentUid)
          AND praActive 
    """)
    abstract suspend fun getAllPeerReviewerAllocations(assignmentUid: List<Long>): List<PeerReviewerAllocation>

    @Query("""
        UPDATE PeerReviewerAllocation 
           SET praActive = :active, 
               praLct = :changeTime
         WHERE praUid = :cbUid""")
    abstract fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateActiveByUid(it, false, changeTime)
        }
    }

}