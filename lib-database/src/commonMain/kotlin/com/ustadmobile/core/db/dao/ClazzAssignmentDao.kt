package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.MAX_VALID_DATE
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.ASSIGNMENT_CLAZZ_UID_CTE_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.WITH_HAS_LEARNINGRECORD_UPDATE_PERMISSION_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.HAS_LEARNINGRECORD_SELECT_PERMISSION_CTE_SQL
import com.ustadmobile.core.db.dao.ClazzAssignmentDaoCommon.SUBMITTER_LIST_CTE2_SQL
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.flow.Flow


@DoorDao
@Repository
expect abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment>, OneToManyJoinDao<ClazzAssignment> {

    @Query("""
     REPLACE INTO ClazzAssignmentReplicate(caPk, caDestination)
      SELECT DISTINCT ClazzAssignment.caUid AS caUid,
             :newNodeId AS caDestination
        FROM UserSession
             JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
             JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         AND ClazzAssignment.caLct != COALESCE(
             (SELECT caVersionId
                FROM ClazzAssignmentReplicate
               WHERE caPk = ClazzAssignment.caUid
                 AND caDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
             SET caPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO ClazzAssignmentReplicate(caPk, caDestination)
  SELECT DISTINCT ClazzAssignment.caUid AS caUid,
         UserSession.usClientNodeId AS caDestination
    FROM ChangeLog
         JOIN ClazzAssignment
             ON ChangeLog.chTableId = ${ClazzAssignment.TABLE_ID}
                AND ChangeLog.chEntityPk = ClazzAssignment.caUid
         JOIN Clazz 
              ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
         ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
              ${Role.PERMISSION_ASSIGNMENT_SELECT}
              ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}  
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND ClazzAssignment.caLct != COALESCE(
         (SELECT caVersionId
            FROM ClazzAssignmentReplicate
           WHERE caPk = ClazzAssignment.caUid
             AND caDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(caPk, caDestination) DO UPDATE
     SET caPending = true
  */               
 """)
    @ReplicationRunOnChange([ClazzAssignment::class])
    @ReplicationCheckPendingNotificationsFor([ClazzAssignment::class])
    abstract suspend fun replicateOnChange()

    @Query("""
        SELECT * 
          FROM ClazzAssignment
         WHERE ClazzAssignment.caClazzUid = :clazzUid
    """)
    abstract suspend fun getAllAssignmentsByClazzUidAsync(clazzUid: Long): List<ClazzAssignment>


    @Query("""
        UPDATE ClazzAssignment 
           SET caActive = :active, 
               caLct = :changeTime
         WHERE caUid = :cbUid""")
    abstract suspend fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)



    @Query("""
        WITH $HAS_LEARNINGRECORD_SELECT_PERMISSION_CTE_SQL,
        $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
        $SUBMITTER_LIST_CTE2_SQL
        
        SELECT 
              -- whether or not the active user has permission to view learner records 
              (SELECT hasPermission
                 FROM HasLearningRecordSelectPermission) AS activeUserHasViewLearnerRecordsPermission,
        
              (SELECT COUNT(*)
                 FROM SubmitterList) AS totalStudents,
              
              -- Total marked students
              (SELECT COUNT(*)
                 FROM SubmitterList
                WHERE EXISTS(
                      SELECT CourseAssignmentMark.camUid
                        FROM CourseAssignmentMark
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                         AND CourseAssignmentMark.camSubmitterUid = SubmitterList.submitterId) 
                ) AS markedStudents,
              
              -- Total who have submitted  
              (SELECT COUNT(*)
                 FROM SubmitterList
                WHERE EXISTS(
                      SELECT CourseAssignmentSubmission.casUid
                        FROM CourseAssignmentSubmission
                       WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                         AND CourseAssignmentSubmission.casSubmitterUid = SubmitterList.submitterId)
                ) AS submittedStudents
          
    """)
    /**
     * Get a summary of the numbers that have submitted/been marked for a given assignment.
     */
    abstract fun getProgressSummaryForAssignment(
        assignmentUid: Long,
        accountPersonUid: Long,
        group: String,
    ): Flow<AssignmentProgressSummary?>

    @Query("""
        WITH $HAS_LEARNINGRECORD_SELECT_PERMISSION_CTE_SQL,
             $ASSIGNMENT_CLAZZ_UID_CTE_SQL,
             $SUBMITTER_LIST_CTE2_SQL
        
        SELECT SubmitterList.name AS name,
               SubmitterList.submitterId AS submitterUid,
               Comments.commentsText AS latestPrivateComment,
               -- Determine submission status - marked, submitted, or not yet submitted
               CASE 
               WHEN CourseAssignmentMark.camUid IS NOT NULL THEN ${CourseAssignmentSubmission.MARKED}
               WHEN CourseAssignmentSubmission.casUid IS NOT NULL THEN ${CourseAssignmentSubmission.SUBMITTED}
               ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} 
               END AS fileSubmissionStatus
               
          FROM SubmitterList
               LEFT JOIN Comments 
                         ON Comments.commentsUid = 
                            (SELECT Comments.commentsUid 
                               FROM Comments
                              WHERE Comments.commentsEntityUid = :assignmentUid
                                AND Comments.commentSubmitterUid = SubmitterList.submitterId
                              LIMIT 1) 
               LEFT JOIN CourseAssignmentMark
                         ON CourseAssignmentMark.camUid = 
                            (SELECT camUid
                               FROM CourseAssignmentMark
                              WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                                AND CourseAssignmentMark.camSubmitterUid = SubmitterList.submitterId
                              LIMIT 1)
               LEFT JOIN CourseAssignmentSubmission
                         ON CourseAssignmentSubmission.casUid = 
                            (SELECT casUid
                               FROM CourseAssignmentSubmission
                              WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                                AND CourseAssignmentSubmission.casSubmitterUid = SubmitterList.submitterId 
                              LIMIT 1)
                    
           
    """)
    /**
     * Used by the ClazzAssignmentDetailSubmissionsListTab - gets a list of the name (e.g. the
     * person name when submissions are by individual students, "group (groupnum)" when submissions
     * are by group.
     *
     * For each submitter, get the status (not submitted, submitted, marked) and the most recent
     * private comment (if any).
     */
    abstract fun getAssignmentSubmitterSummaryListForAssignment(
        assignmentUid: Long,
        accountPersonUid: Long,
        group: String,
    ): Flow<List<AssignmentSubmitterSummary>>

    /**
     * Used by UpdatePeerReviewAllocationUseCase - which needs to run even before the assignment
     * is saved to the database. Finds a list of all the expected submitter ids - list of enrolled
     * student personuids if the assignment is for individual submission, list of group numbers if
     * assignment is by groups.
     */
    @Query("""
         -- Submitter UIDs for individual assignment the list of personuids enrolled in the course
         SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterUid
           FROM ClazzEnrolment
          WHERE (:groupSetUid = 0)
            AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
            AND :time BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined AND ClazzEnrolment.clazzEnrolmentDateLeft
          
         UNION
         
        SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterUid
          FROM CourseGroupMember
         WHERE :groupSetUid != 0
           AND CourseGroupMember.cgmSetUid = :groupSetUid         
    """)
    abstract suspend fun getSubmitterUidsByClazzOrGroupSetUid(
        clazzUid: Long,
        groupSetUid: Long,
        time: Long
    ): List<Long>


    @Query("""
        $WITH_HAS_LEARNINGRECORD_UPDATE_PERMISSION_SQL
        
        SELECT (CASE WHEN caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER}
                    THEN (SELECT hasPermission FROM AssignmentPermission)
                    ELSE PeerReviewerAllocation.praUid IS NOT NULL END)
          FROM ClazzAssignment
              
               LEFT JOIN PeerReviewerAllocation
               ON PeerReviewerAllocation.praToMarkerSubmitterUid = :selectedPersonUid
               AND PeerReviewerAllocation.praMarkerSubmitterUid = :submitterUid
               AND praActive
         WHERE caUid = :caUid 
    """)
    abstract suspend fun canMarkAssignment(
        caUid: Long,
        clazzUid: Long,
        loggedInPersonUid: Long,
        submitterUid: Long,
        selectedPersonUid: Long): Boolean



    @Query("""
         $SUBMITTER_LIST_WITHOUT_ASSIGNMENT_CTE
        
         SELECT COUNT(*) 
          FROM SubmitterList
    """)
    abstract suspend fun getSubmitterCountFromAssignment(
        groupUid: Long,
        clazzUid: Long,
        group: String
    ): Int


    @Query("""
        $SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL
    """)
    abstract suspend fun getSubmitterUid(assignmentUid: Long, accountPersonUid: Long): Long

    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?


    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract fun findByUidAsFlow(uid: Long): Flow<ClazzAssignment?>

    @Query("""
        SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          :permission
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = 
                       (SELECT caClazzUid 
                          FROM ClazzAssignment
                         WHERE caUid = :clazzAssignmentUid)
                   AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid)
    """)
    abstract fun personHasPermissionWithClazzByAssignmentUidAsFlow(
        accountPersonUid: Long,
        clazzAssignmentUid: Long,
        permission: Long
    ): Flow<Boolean>

    @Query("""
          SELECT COALESCE((
           SELECT caGroupUid
           FROM ClazzAssignment
          WHERE caUid = :uid),-1)
    """)
    abstract suspend fun getGroupUidFromAssignment(uid: Long): Long

    @Query("""
          SELECT COALESCE((
           SELECT caMarkingType
           FROM ClazzAssignment
          WHERE caUid = :uid),-1)
    """)
    abstract suspend fun getMarkingTypeFromAssignment(uid: Long): Int

    @Query("""
        SELECT * 
          FROM ClazzAssignment
               LEFT JOIN CourseBlock
               ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidWithBlockAsync(uid: Long): ClazzAssignmentWithCourseBlock?

    @Query("""
        SELECT * 
          FROM ClazzAssignment LIMIT 1
    """)
    abstract fun findClazzAssignment(): ClazzAssignment?

    @Query("""SELECT * 
                      FROM ClazzAssignment 
                     WHERE caUid = :uid""")
    abstract fun findByUidLive(uid: Long): LiveData<ClazzAssignment?>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertListAsync(entities: List<ClazzAssignment>)

    @Query("""
        UPDATE ClazzAssignment
           SET caActive = :active,
               caLct = :changeTime
         WHERE caUid IN (:uidList)   
    """)
    abstract suspend fun updateActiveByList(
        uidList: List<Long>,
        active: Boolean,
        changeTime: Long
    )

    @Query("""
        WITH PersonIsStudent(isStudent)
             AS (SELECT EXISTS(
                        SELECT ClazzEnrolment.clazzEnrolmentPersonUid
                           FROM ClazzEnrolment
                          WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = 
                                (SELECT caClazzUid 
                                   FROM ClazzAssignment
                                  WHERE caUid = :assignmentUid) 
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}))
                        
        SELECT ClazzAssignment.*,
               CourseBlock.*,
               ($SELECT_SUBMITTER_UID_FOR_PERSONUID_AND_ASSIGNMENTUID_SQL) AS submitterUid
                   
          FROM ClazzAssignment
               JOIN CourseBlock
                    ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
         WHERE ClazzAssignment.caUid = :assignmentUid           
    """)
    abstract fun findAssignmentCourseBlockAndSubmitterUidAsFlow(
        assignmentUid: Long,
        accountPersonUid: Long,
    ): Flow<ClazzAssignmentCourseBlockAndSubmitterUid?>


    @Query("""
        WITH CourseBlockDeadlines(deadline, gracePeriod) AS
             (SELECT CourseBlock.cbDeadlineDate AS deadline,
                     CourseBlock.cbGracePeriodDate AS gracePeriod
                FROM CourseBlock
               WHERE CourseBlock.cbEntityUid = :assignmentUid
                 AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
               LIMIT 1)
        SELECT CASE
               WHEN (SELECT gracePeriod 
                       FROM CourseBlockDeadlines)
                    BETWEEN 1 AND $MAX_VALID_DATE THEN (SELECT gracePeriod FROM CourseBlockDeadlines)
               ELSE (SELECT deadline FROM CourseBlockDeadlines)
               END AS latestSubmissionTimeAllowed
    """)
    abstract suspend fun getLatestSubmissionTimeAllowed(assignmentUid: Long): Long
}