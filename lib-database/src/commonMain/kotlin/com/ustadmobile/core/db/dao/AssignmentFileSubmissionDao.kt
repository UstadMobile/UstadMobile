package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AssignmentFileSubmission
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession

@Dao
@Repository
abstract class AssignmentFileSubmissionDao : BaseDao<AssignmentFileSubmission> {

    @Query("""
     REPLACE INTO AssignmentFileSubmissionReplicate(afsPk, afsDestination)
      SELECT DISTINCT AssignmentFileSubmission.afsUid AS afsUid,
             :newNodeId AS afsDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid                
             JOIN AssignmentFileSubmission
                    ON AssignmentFileSubmission.afsAssignmentUid = ClazzAssignment.caUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND AssignmentFileSubmission.afsLct != COALESCE(
             (SELECT afsVersionId
                FROM AssignmentFileSubmissionReplicate
               WHERE afsPk = AssignmentFileSubmission.afsUid
                 AND afsDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(afsPk, afsDestination) DO UPDATE
             SET afsPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([AssignmentFileSubmission::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO AssignmentFileSubmissionReplicate(afsPk, afsDestination)
  SELECT DISTINCT AssignmentFileSubmission.afsUid AS afsUid,
         UserSession.usClientNodeId AS afsDestination
    FROM ChangeLog
         JOIN AssignmentFileSubmission
             ON ChangeLog.chTableId = ${AssignmentFileSubmission.TABLE_ID}
                AND ChangeLog.chEntityPk = AssignmentFileSubmission.afsUid
             JOIN ClazzAssignment
                    ON AssignmentFileSubmission.afsAssignmentUid = ClazzAssignment.caUid
             JOIN Clazz
                    ON  Clazz.clazzUid = ClazzAssignment.caClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND AssignmentFileSubmission.afsLct != COALESCE(
         (SELECT afsVersionId
            FROM AssignmentFileSubmissionReplicate
           WHERE afsPk = AssignmentFileSubmission.afsUid
             AND afsDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(afsPk, afsDestination) DO UPDATE
     SET afsPending = true
  */               
 """)
    @ReplicationRunOnChange([AssignmentFileSubmission::class])
    @ReplicationCheckPendingNotificationsFor([AssignmentFileSubmission::class])
    abstract suspend fun replicateOnChange()


    @Query("""
        SELECT * 
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive
    """)
    abstract fun getAllFileSubmissionsFromStudent(assignmentUid: Long, studentUid: Long)
            : DoorDataSourceFactory<Int, AssignmentFileSubmission>

    @Query("""
        SELECT * 
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive
           AND afsSubmitted
    """)
    abstract fun getAllSubmittedFileSubmissionsFromStudent(assignmentUid: Long, studentUid: Long)
            : DoorDataSourceFactory<Int, AssignmentFileSubmission>


    @Update
    abstract suspend fun updateAsync(entity: AssignmentFileSubmission): Int


    @Query("""
        UPDATE AssignmentFileSubmission
           SET afsSubmitted = :submit, afsLct = :currentTime,
               afsTimestamp = :currentTime
         WHERE afsAssignmentUid = :assignmentUid
           AND afsStudentUid = :studentUid
           AND afsActive 
           AND NOT afsSubmitted
    """)
    abstract suspend fun setFilesAsSubmittedForStudent(assignmentUid: Long, studentUid: Long,
                                                       submit: Boolean, currentTime: Long)


    @Query("""
        SELECT afsStudentUid
          FROM AssignmentFileSubmission
         WHERE afsAssignmentUid = :assignmentUid
           AND afsSubmitted 
           AND afsActive
         LIMIT 1
    """)
    abstract suspend fun findNextStudentToGrade(assignmentUid: Long): Long
}