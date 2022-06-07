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
           AND casSubmitterUid = :submitterUid
      ORDER BY casTimestamp DESC
    """)
    abstract fun getAllSubmissionsFromSubmitter(assignmentUid: Long, submitterUid: Long)
            : DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>

    @Query("""
        SELECT Count(casUid)
          FROM CourseAssignmentSubmission
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :submitterUid
           AND casType = ${CourseAssignmentSubmission.SUBMISSION_TYPE_FILE}
    """)
    abstract suspend fun countFileSubmissionFromStudent(assignmentUid: Long, submitterUid: Long): Int

    @Query("""
        SELECT Count(casUid)
          FROM CourseAssignmentSubmission
         WHERE casAssignmentUid = :assignmentUid
           AND casSubmitterUid = :submitterUid
    """)
    abstract suspend fun countSubmissionsFromSubmitter(assignmentUid: Long, submitterUid: Long): Int

    @Query("""
           SELECT COALESCE((
                SELECT (CASE WHEN CourseAssignmentMark.camAssignmentUid IS NOT NULL 
                             THEN ${CourseAssignmentSubmission.MARKED}
                             ELSE ${CourseAssignmentSubmission.SUBMITTED} 
                             END) AS status
                  FROM CourseAssignmentSubmission
                       
                       LEFT JOIN CourseAssignmentMark
                       ON CourseAssignmentMark.camAssignmentUid = :assignmentUid
                       AND CourseAssignmentMark.camSubmitterUid = :submitterUid
                       
                 WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                   AND CourseAssignmentSubmission.casSubmitterUid = :submitterUid
                 LIMIT 1
           ),${CourseAssignmentSubmission.NOT_SUBMITTED}) AS Status
    """)
    abstract fun getStatusOfAssignmentForSubmitter(assignmentUid: Long, submitterUid: Long): DoorLiveData<Int>

    @Query("""
        SELECT * 
          FROM CourseAssignmentSubmission
         WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
           AND CourseAssignmentSubmission.casSubmitterUid = :submitterUid
      ORDER BY casTimestamp DESC
         LIMIT 1
    """)
    abstract suspend fun findLastSubmissionFromStudent(submitterUid: Long, assignmentUid: Long): CourseAssignmentSubmission?


    @Query("""
         SELECT NOT EXISTS(SELECT 1
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       LIMIT 1)
    """)
    abstract fun checkNoSubmissionsMade(assignmentUid: Long): DoorLiveData<Boolean>

    companion object {

        const val GET_SUBMITTERID_FROM_STUDENT = """
             (CASE WHEN ClazzAssignment.caGroupUid = 0
                                       THEN :studentUid
                                       ELSE COALESCE((SELECT cgmGroupNumber 
                                                       FROM CourseGroupMember
                                                      WHERE cgmSetUid = ClazzAssignment.caGroupUid
                                                        AND cgmPersonUid = :studentUid
                                                      LIMIT 1),0))
        """

    }

}