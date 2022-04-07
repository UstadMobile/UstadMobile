package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@Dao
@Repository
abstract class CourseAssignmentSubmissionDao : BaseDao<CourseAssignmentSubmission> {

    @Query("""
     REPLACE INTO CourseAssignmentSubmissionReplicate(casPk, casDestination)
      SELECT DISTINCT CourseAssignmentSubmission.casUid AS casPk,
             :newNodeId AS casDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid                
             JOIN CourseAssignmentSubmission
                    ON CourseAssignmentSubmission.casAssignmentUid = ClazzAssignment.caUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseAssignmentSubmission.casTimestamp != COALESCE(
             (SELECT casVersionId
                FROM CourseAssignmentSubmissionReplicate
               WHERE casPk = CourseAssignmentSubmission.casUid
                 AND casDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(casPk, casDestination) DO UPDATE
             SET casPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentSubmission::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseAssignmentSubmissionReplicate(casPk, casDestination)
  SELECT DISTINCT CourseAssignmentSubmission.casUid AS casPk,
         UserSession.usClientNodeId AS casDestination
    FROM ChangeLog
         JOIN CourseAssignmentSubmission
             ON ChangeLog.chTableId = ${CourseAssignmentSubmission.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseAssignmentSubmission.casUid
             JOIN ClazzAssignment
                    ON CourseAssignmentSubmission.casAssignmentUid = ClazzAssignment.caUid
             JOIN Clazz
                    ON  Clazz.clazzUid = ClazzAssignment.caClazzUid
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND CourseAssignmentSubmission.casTimestamp != COALESCE(
         (SELECT casVersionId
            FROM CourseAssignmentSubmissionReplicate
           WHERE casPk = CourseAssignmentSubmission.casUid
             AND casDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(casPk, casDestination) DO UPDATE
     SET casPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseAssignmentSubmission::class])
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentSubmission::class])
    abstract suspend fun replicateOnChange()

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseAssignmentSubmission>)


    @Query("""
        SELECT * 
          FROM CourseAssignmentSubmission
               LEFT JOIN CourseAssignmentSubmissionAttachment
               ON CourseAssignmentSubmissionAttachment.casaSubmissionUid = CourseAssignmentSubmission.casUid
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :studentUid
      ORDER BY casTimestamp DESC
    """)
    abstract fun getAllFileSubmissionsFromStudent(assignmentUid: Long, studentUid: Long)
            : DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>

    @Query("""
           SELECT COALESCE((
                SELECT (CASE WHEN CourseAssignmentMark.camAssignmentUid IS NOT NULL 
                             THEN ${CourseAssignmentSubmission.MARKED}
                             ELSE ${CourseAssignmentSubmission.SUBMITTED} 
                             END) AS status
                  FROM CourseAssignmentSubmission
                       LEFT JOIN CourseAssignmentMark
                       ON CourseAssignmentMark.camAssignmentUid = :assignmentUid
                       AND CourseAssignmentMark.camStudentUid = :studentUid
                 WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                   AND CourseAssignmentSubmission.casSubmitterUid = :studentUid
                 LIMIT 1
           ),${CourseAssignmentSubmission.NOT_SUBMITTED}) AS Status
    """)
    abstract fun getStatusOfAssignmentForStudent(assignmentUid: Long, studentUid: Long): DoorLiveData<Int>

    @Query("""
        SELECT * 
          FROM CourseAssignmentSubmission
         WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
           AND CourseAssignmentSubmission.casSubmitterUid = :studentUid
      ORDER BY casTimestamp DESC
         LIMIT 1
    """)
    abstract suspend fun findLastSubmissionFromStudent(studentUid: Long, assignmentUid: Long): CourseAssignmentSubmission?

}