package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class CourseAssignmentMarkDao : BaseDao<CourseAssignmentMark> {


    @Query("""
     REPLACE INTO CourseAssignmentMarkReplicate(camPk, camDestination)
      SELECT DISTINCT CourseAssignmentMark.camUid AS camPk,
             :newNodeId AS camDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid                
             JOIN CourseAssignmentMark
                    ON CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
                    
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseAssignmentMark.camLct != COALESCE(
             (SELECT camVersionId
                FROM CourseAssignmentMarkReplicate
               WHERE camPk = CourseAssignmentMark.camUid
                 AND camDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(camPk, camDestination) DO UPDATE
             SET camPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentMark::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseAssignmentMarkReplicate(camPk, camDestination)
  SELECT DISTINCT CourseAssignmentMark.camUid AS camPk,
         UserSession.usClientNodeId AS camDestination
    FROM ChangeLog
         JOIN CourseAssignmentMark
             ON ChangeLog.chTableId = ${CourseAssignmentMark.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseAssignmentMark.camUid
             JOIN ClazzAssignment
                    ON CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
             JOIN Clazz
                    ON  Clazz.clazzUid = ClazzAssignment.caClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseAssignmentMark.camLct != COALESCE(
         (SELECT camVersionId
            FROM CourseAssignmentMarkReplicate
           WHERE camPk = CourseAssignmentMark.camUid
             AND camDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(camPk, camDestination) DO UPDATE
     SET camPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseAssignmentMark::class])
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentMark::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        SELECT * 
          FROM CourseAssignmentMark               
         WHERE camAssignmentUid = :assignmentUid
           AND camSubmitterUid = :submitterUid
      ORDER BY camLct DESC
         LIMIT 1
    """)
    abstract fun getMarkOfAssignmentForSubmitterLiveData(assignmentUid: Long, submitterUid: Long): LiveData<CourseAssignmentMark?>

    @Query("""
        SELECT * 
          FROM CourseAssignmentMark
         WHERE camAssignmentUid = :assignmentUid
           AND camSubmitterUid = :submitterUid
      ORDER BY camLct DESC
         LIMIT 1
    """)
    abstract fun getMarkOfAssignmentForStudent(assignmentUid: Long, submitterUid: Long): CourseAssignmentMark?


    @Query("""
         SELECT COALESCE((
            SELECT casSubmitterUid
              FROM CourseAssignmentSubmission
              
                   LEFT JOIN CourseAssignmentMark
                   ON CourseAssignmentMark.camSubmitterUid = CourseAssignmentSubmission.casSubmitterUid
                   AND CourseAssignmentMark.camAssignmentUid = :assignmentUid
                   
             WHERE CourseAssignmentSubmission.casSubmitterUid != :submitterUid
               AND CourseAssignmentMark.camUid IS NULL
          GROUP BY casSubmitterUid
         LIMIT 1),0)
    """)
    abstract suspend fun findNextSubmitterToMarkForAssignment(assignmentUid: Long, submitterUid: Long): Long
}