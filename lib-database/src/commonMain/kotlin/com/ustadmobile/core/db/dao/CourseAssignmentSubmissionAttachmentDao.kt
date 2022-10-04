package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionAttachment
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UserSession
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class CourseAssignmentSubmissionAttachmentDao : BaseDao<CourseAssignmentSubmissionAttachment> {

    @Query("""
     REPLACE INTO CourseAssignmentSubmissionAttachmentReplicate(casaPk, casaDestination)
      SELECT DISTINCT CourseAssignmentSubmissionAttachment.casaUid AS casaPk,
             :newNodeId AS casaDestination
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
             JOIN CourseAssignmentSubmissionAttachment
                  ON CourseAssignmentSubmissionAttachment.casaSubmissionUid = CourseAssignmentSubmission.casUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND CourseAssignmentSubmissionAttachment.casaTimestamp != COALESCE(
             (SELECT casaVersionId
                FROM CourseAssignmentSubmissionAttachmentReplicate
               WHERE casaPk = CourseAssignmentSubmissionAttachment.casaUid
                 AND casaDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(casaPk, casaDestination) DO UPDATE
             SET casaPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentSubmissionAttachment::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO CourseAssignmentSubmissionAttachmentReplicate(casaPk, casaDestination)
  SELECT DISTINCT CourseAssignmentSubmissionAttachment.casaUid AS casaPk,
         UserSession.usClientNodeId AS casaDestination
    FROM ChangeLog
         JOIN CourseAssignmentSubmissionAttachment
             ON ChangeLog.chTableId = ${CourseAssignmentSubmissionAttachment.TABLE_ID}
                AND ChangeLog.chEntityPk = CourseAssignmentSubmissionAttachment.casaUid
             JOIN CourseAssignmentSubmission
                  ON CourseAssignmentSubmissionAttachment.casaSubmissionUid = CourseAssignmentSubmission.casUid   
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
     AND CourseAssignmentSubmissionAttachment.casaTimestamp != COALESCE(
         (SELECT casaVersionId
            FROM CourseAssignmentSubmissionAttachmentReplicate
           WHERE casaPk = CourseAssignmentSubmissionAttachment.casaUid
             AND casaDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(casaPk, casaDestination) DO UPDATE
     SET casaPending = true
  */               
 """)
    @ReplicationRunOnChange([CourseAssignmentSubmissionAttachment::class])
    @ReplicationCheckPendingNotificationsFor([CourseAssignmentSubmissionAttachment::class])
    abstract suspend fun replicateOnChange()

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<CourseAssignmentSubmissionAttachment>)


}