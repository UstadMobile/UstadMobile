package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*

@Dao
@Repository
abstract class CourseAssignmentMarkDao : BaseDao<CourseAssignmentMark> {


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
           AND camStudentUid = :studentUid
      ORDER BY camLct DESC
         LIMIT 1
    """)
    abstract fun getMarkOfAssignmentForStudentLiveData(assignmentUid: Long, studentUid: Long): DoorLiveData<CourseAssignmentMark?>

    @Query("""
        SELECT * 
          FROM CourseAssignmentMark
         WHERE camAssignmentUid = :assignmentUid
           AND camStudentUid = :studentUid
      ORDER BY camLct DESC
         LIMIT 1
    """)
    abstract fun getMarkOfAssignmentForStudent(assignmentUid: Long, studentUid: Long): CourseAssignmentMark?


    @Query("""
         SELECT COALESCE((
            SELECT clazzEnrolmentPersonUid
              FROM ClazzEnrolment
                  JOIN CourseAssignmentSubmission
                  ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentSubmission.casSubmitterUid
                  AND CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
              
                   LEFT JOIN CourseAssignmentMark
                   ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentMark.camStudentUid
                   AND CourseAssignmentMark.camAssignmentUid = :assignmentUid
               WHERE ClazzEnrolment.clazzEnrolmentActive
               AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
               AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
               AND ClazzEnrolment.clazzEnrolmentPersonUid != :studentUid
               AND CourseAssignmentMark.camUid IS NULL
         LIMIT 1),0)
    """)
    abstract suspend fun findNextStudentToMarkForAssignment(assignmentUid: Long, studentUid: Long): Long
}