package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment>, OneToManyJoinDao<ClazzAssignment> {

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

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateActiveByUid(it, false, changeTime)
        }
    }

    @Query("""
            $SUBMITTER_LIST_CTE
            
            SELECT (SELECT COUNT(*) FROM SubmitterList) AS totalStudents,
            
                    0 as notSubmittedStudents,
                    
                    (SELECT COUNT(DISTINCT CourseAssignmentSubmission.casSubmitterUid) 
                      FROM CourseAssignmentSubmission
                           LEFT JOIN CourseAssignmentMark
                           ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                           AND CourseAssignmentMark.camAssignmentUid = CourseAssignmentSubmission.casAssignmentUid
                     WHERE CourseAssignmentSubmission.casAssignmentUid = :assignmentUid
                       AND CourseAssignmentMark.camUid IS NULL
                       AND CourseAssignmentSubmission.casSubmitterUid IN (SELECT submitterId 
                                                                            FROM SubmitterList))
                      AS submittedStudents,
                     
                     
                     (SELECT COUNT(DISTINCT CourseAssignmentMark.camSubmitterUid) 
                        FROM CourseAssignmentMark
                            
                             JOIN CourseAssignmentSubmission
                             ON CourseAssignmentSubmission.casSubmitterUid = CourseAssignmentMark.camSubmitterUid
                             AND CourseAssignmentSubmission.casAssignmentUid = CourseAssignmentMark.camAssignmentUid
                             
                       WHERE CourseAssignmentMark.camAssignmentUid = :assignmentUid
                         AND CourseAssignmentMark.camSubmitterUid IN (SELECT submitterId 
                                                                            FROM SubmitterList))
                         AS markedStudents, 
                         
                         'TRUE' AS hasMetricsPermission
                         
         FROM  ClazzAssignment
        WHERE caActive
          AND caClazzUid = :clazzUid 
          AND caUid = :assignmentUid                  
    """)
    abstract fun getProgressSummaryForAssignment(
        assignmentUid: Long, clazzUid: Long, group: String) : DoorLiveData<AssignmentProgressSummary?>


    @Query("""
         $SUBMITTER_LIST_CTE
        
         SELECT submitterId AS submitterUid,
                name, 
                
                 COALESCE((CASE WHEN CourseAssignmentMark.camUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.MARKED} 
                          WHEN CourseAssignmentSubmission.casUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.SUBMITTED} 
                          ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} END), 
                               ${CourseAssignmentSubmission.NOT_SUBMITTED}) AS fileSubmissionStatus,
                
                (CASE WHEN ClazzAssignment.caGroupUid = 0 
                 THEN 'TRUE' 
                 ELSE 'FALSE' END) AS isGroupAssignment,
                 
                 cm.commentsText AS latestPrivateComment 

           FROM SubmitterList
                JOIN ClazzAssignment
                ON ClazzAssignment.caUid = :assignmentUid
           
                LEFT JOIN CourseAssignmentMark
                ON CourseAssignmentMark.camUid = (SELECT camUid
                                                    FROM CourseAssignmentMark
                                                   WHERE camAssignmentUid = :assignmentUid
                                                     AND camSubmitterUid = SubmitterList.submitterId
                                                ORDER BY camLct DESC 
                                                   LIMIT 1)
                
                LEFT JOIN CourseAssignmentSubmission
                ON CourseAssignmentSubmission.casUid = (SELECT casUid
                                                          FROM CourseAssignmentSubmission
                                                         WHERE casAssignmentUid = :assignmentUid
                                                           AND casSubmitterUid = SubmitterList.submitterId
                                                      ORDER BY casTimestamp DESC 
                                                         LIMIT 1)
                LEFT JOIN Comments AS cm 
                    ON cm.commentsUid = (
                                 SELECT Comments.commentsUid 
                                   FROM Comments 
                                  WHERE Comments.commentsEntityType = ${ClazzAssignment.TABLE_ID}
                                    AND commentsEntityUid = :assignmentUid
                                    AND NOT commentsInActive
                                    AND NOT commentsPublic
                                    AND (CASE WHEN ClazzAssignment.caGroupUid = 0
                                              THEN commentsPersonUid = SubmitterList.submitterId
                                              ELSE commentSubmitterUid = SubmitterList.submitterId END)
                               ORDER BY commentsDateTimeAdded DESC LIMIT 1)                                                      
                                                                      
          WHERE name LIKE :searchText
       ORDER BY name 
    """)
    abstract fun getSubmitterListForAssignment(
        assignmentUid: Long,
        clazzUid: Long,
        group: String,
        searchText: String
    ): DoorDataSourceFactory<Int, PersonGroupAssignmentSummary>


    @Query("""
        SELECT (CASE WHEN ClazzAssignment.caGroupUid = 0 
                     THEN :personUid 
                     WHEN CourseGroupMember.cgmUid IS NULL 
                     THEN 0 
                     ELSE CourseGroupMember.cgmGroupNumber END) as submitterUid
          FROM ClazzAssignment
               LEFT JOIN CourseGroupMember
               ON cgmSetUid = ClazzAssignment.caGroupUid
               AND cgmPersonUid = :personUid
         WHERE caUid = :assignmentUid
    """)
    abstract suspend fun getSubmitterUid(assignmentUid: Long, personUid: Long): Long

    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?


    @Query("""
          SELECT COALESCE((
           SELECT caGroupUid
           FROM ClazzAssignment
          WHERE caUid = :uid),-1)
    """)
    abstract suspend fun getGroupUidFromAssignment(uid: Long): Long

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
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzAssignment?>

    companion object{

        const val SUBMITTER_LIST_CTE = """
            WITH SubmitterList (submitterId, name)
            AS (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid AS submitterId, 
                       Person.firstNames || ' ' || Person.lastName AS name
                  FROM ClazzEnrolment
                  
                       JOIN Person 
                       ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
                        
                       JOIN ClazzAssignment
                       ON ClazzAssignment.caUid = :assignmentUid

                       JOIN CourseBlock
                       ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
                       AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                       
                 WHERE ClazzAssignment.caGroupUid = 0
                   AND clazzEnrolmentClazzUid = :clazzUid
                   AND clazzEnrolmentActive
                   AND clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND CourseBlock.cbGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft
                   AND ClazzEnrolment.clazzEnrolmentDateJoined <= CourseBlock.cbGracePeriodDate
              GROUP BY submitterId, name
            UNION                 
             SELECT DISTINCT CourseGroupMember.cgmGroupNumber AS submitterId,
                    :group || ' ' || CourseGroupMember.cgmGroupNumber AS name  
               FROM CourseGroupMember
                    JOIN ClazzAssignment
                    ON ClazzAssignment.caUid = :assignmentUid
              WHERE CourseGroupMember.cgmSetUid = ClazzAssignment.caGroupUid
                AND ClazzAssignment.caGroupUid != 0
                AND CourseGroupMember.cgmGroupNumber != 0
           GROUP BY submitterId, name
            )
        """

        const val SORT_DEADLINE_ASC = 1

        const val SORT_DEADLINE_DESC = 2

        const val SORT_TITLE_ASC = 3

        const val SORT_TITLE_DESC = 4

        const val SORT_SCORE_ASC = 5

        const val SORT_SCORE_DESC = 6

        const val SORT_START_DATE_ASC = 7

        const val SORT_START_DATE_DESC = 8

    }

}