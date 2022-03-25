package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_DESC
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
    abstract fun updateActiveByUid(cbUid: Long, active: Boolean,  changeTime: Long)

    override suspend fun deactivateByUids(uidList: List<Long>, changeTime: Long) {
        uidList.forEach {
            updateActiveByUid(it, false, changeTime)
        }
    }

    @Query("""
        SELECT 0 AS resultMax, 
               0 AS resultScore, 
               COALESCE(SUM(ResultSource.cacheFinalWeightScoreWithPenalty),0) as resultScaled,
               'FALSE' as contentComplete, 
               COALESCE(AVG(ResultSource.cacheProgress),0) as progress, 0 as success,
               0 AS penalty,
               
               0 As resultWeight,
               
              COALESCE((SUM(CASE 
                        WHEN CAST(ResultSource.cacheContentComplete AS INTEGER) > 0 
                        THEN 1 ELSE 0 END)), 0) AS totalCompletedContent,
                        
               COALESCE(COUNT(DISTINCT ResultSource.cacheContentEntryUid), 0) AS totalContent
 
     	  FROM (SELECT ClazzAssignmentRollUp.cacheContentComplete, ClazzAssignmentRollUp.cacheProgress,
                        ClazzAssignmentRollUp.cacheContentEntryUid, ClazzAssignmentRollUp.cacheWeight, 
                        ClazzAssignmentRollUp.cacheFinalWeightScoreWithPenalty
     	 	      FROM ClazzAssignmentRollUp 
                 WHERE ClazzAssignmentRollUp.cachePersonUid = :personUid
                   AND ClazzAssignmentRollUp.cacheClazzAssignmentUid = :caUid
              GROUP BY ClazzAssignmentRollUp.cacheContentEntryUid
     	  ) AS ResultSource
    """)
    @SqliteOnly
    abstract fun getStatementScoreProgressForAssignment(caUid: Long, personUid: Long): DoorLiveData<ContentEntryStatementScoreProgress?>
    
    @Query("""
         SELECT ResultSource.personUid, ResultSource.firstNames, ResultSource.lastName,
            0 AS attempts, 
            0 AS startDate, 
            0 AS endDate, 
            0 AS duration, 
            
            
            0 AS progress,
                
                0 as success, 
                
              0 AS resultScaled,
                              
             0 AS resultWeight,       
                
                'FALSE' as contentComplete,
                
                0 AS resultScore,
                          
                0 AS resultMax, 
                                        
                0 AS penalty,   
                                                    
                  0 AS totalCompletedContent,
            
           0 AS totalContent,                              
                                                   
           COALESCE((CASE WHEN ResultSource.camUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.MARKED} 
                          WHEN ResultSource.casUid IS NOT NULL 
                          THEN ${CourseAssignmentSubmission.SUBMITTED} 
                          ELSE ${CourseAssignmentSubmission.NOT_SUBMITTED} END), 
                               ${CourseAssignmentSubmission.NOT_SUBMITTED}) AS fileSubmissionStatus,
                                 

            cm.commentsText AS latestPrivateComment
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            CourseAssignmentSubmission.casUid, 
            CourseAssignmentMark.camUid
                FROM PersonGroupMember
         ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             LEFT JOIN ClazzEnrolment
             ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid 
                AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
            
             LEFT JOIN ClazzAssignment 
             ON ClazzAssignment.caUid = :assignmentUid
             
             LEFT JOIN CourseAssignmentSubmission
             ON CourseAssignmentSubmission.casAssignmentUid = ClazzAssignment.caUid
             AND CourseAssignmentSubmission.casStudentUid = Person.personUid
             
             LEFT JOIN CourseAssignmentMark
             ON CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
             AND CourseAssignmentMark.camStudentUid = Person.personUid
                
                WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                AND PersonGroupMember.groupMemberActive                      
                AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
                AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}     
                AND ClazzEnrolment.clazzEnrolmentActive
                GROUP BY Person.personUid) AS ResultSource 
             		LEFT JOIN Comments AS cm 
                    ON cm.commentsUid = (
                                 SELECT Comments.commentsUid 
                                   FROM Comments 
                                  WHERE Comments.commentsEntityType = ${ClazzAssignment.TABLE_ID}
                                    AND commentsEntityUid = :assignmentUid
                                    AND NOT commentsInActive
                                    AND NOT commentsPublic
                                    AND Comments.commentsPersonUid = ResultSource.personUid
                               ORDER BY commentsDateTimeAdded DESC LIMIT 1)

         GROUP BY ResultSource.personUid 
         ORDER BY CASE(:sortOrder) 
                WHEN $SORT_FIRST_NAME_ASC THEN ResultSource.firstNames
                WHEN $SORT_LAST_NAME_ASC THEN ResultSource.lastName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_FIRST_NAME_DESC THEN ResultSource.firstNames
                WHEN $SORT_LAST_NAME_DESC THEN ResultSource.lastName
                ELSE ''
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_LAST_ACTIVE_ASC THEN endDate 
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_LAST_ACTIVE_DESC then endDate
                ELSE 0
            END DESC
    """)
    @SqliteOnly
    abstract fun getAttemptSummaryForStudentsInAssignment(assignmentUid: Long, clazzUid: Long,
                                                          accountPersonUid: Long,
                                                          searchText: String, sortOrder: Int):
            DoorDataSourceFactory<Int, PersonWithAttemptsSummary>

    @Query("""
        WITH CtePermissionCheck (hasPermission) 
            AS (SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          :permission
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid))
                
                
        SELECT (SELECT hasPermission FROM CtePermissionCheck) AS hasMetricsPermission,
        
        (SELECT COUNT(*) 
                        FROM ClazzEnrolment 
                        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid 
                        AND ClazzEnrolment.clazzEnrolmentActive 
                        AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                        AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft) 
                        AS totalStudents, 
        
               0 AS notSubmittedStudents,
                
               (CASE WHEN (SELECT hasPermission 
                          FROM CtePermissionCheck)
                     THEN (SELECT COUNT(DISTINCT CourseAssignmentSubmission.casStudentUid)
                         FROM ClazzEnrolment
                              JOIN CourseAssignmentSubmission
                              ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentSubmission.casStudentUid
                              AND ClazzAssignment.caUid = CourseAssignmentSubmission.casAssignmentUid
                             
                              LEFT JOIN CourseAssignmentMark
                              ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentMark.camStudentUid
                              AND ClazzAssignment.caUid = CourseAssignmentMark.camAssignmentUid
                              
                        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                          AND ClazzEnrolment.clazzEnrolmentActive
                          AND CourseAssignmentMark.camUid IS NULL
                          AND ClazzAssignment.caClazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                          AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft) 
                ELSE 0 END) AS submittedStudents,      

                  
             (CASE WHEN (SELECT hasPermission 
                           FROM CtePermissionCheck)
                   THEN (SELECT COUNT(DISTINCT(CourseAssignmentMark.camStudentUid)) 
                           FROM CourseAssignmentMark 
                                JOIN ClazzEnrolment
                                ON ClazzEnrolment.clazzEnrolmentPersonUid = CourseAssignmentMark.camStudentUid
                                
                          WHERE CourseAssignmentMark.camAssignmentUid = ClazzAssignment.caUid
                            AND ClazzEnrolment.clazzEnrolmentActive
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                            AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft)
                   ELSE 0 END) AS markedStudents
                           
        
        FROM ClazzAssignment
       WHERE caActive
         AND caClazzUid = :clazzUid 
         AND caUid = :clazzAssignmentUid
    """)
    abstract fun getStudentsProgressOnAssignment(clazzUid: Long, accountPersonUid: Long,
                                                 clazzAssignmentUid: Long, permission: Long): DoorLiveData<AssignmentProgressSummary?>



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
          FROM ClazzAssignment LIMIT 1
    """)
    abstract fun findClazzAssignment(): ClazzAssignment?

    @Query("""SELECT * 
                      FROM ClazzAssignment 
                     WHERE caUid = :uid""")
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzAssignment?>

    companion object{

        private const val GET_TOTAL_SCORE_WITH_PENALTY_FOR_USER_IN_ASSIGNMENT = """
               COALESCE((SELECT SUM(cacheFinalWeightScoreWithPenalty)
                          FROM ClazzAssignmentRollUp
                         WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                          AND cachePersonUid = :accountPersonUid), 0)

        """

        private const val GET_TOTAL_WEIGHT_OF_ASSIGNMENT = """
            COALESCE((SELECT SUM(cacheWeight)
                        FROM ClazzAssignmentRollUp
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid), 0)
        """


        private const val GET_TOTAL_CONTENT_OF_ASSIGNMENT = """
            COALESCE((SELECT COUNT(DISTINCT cacheContentEntryUid)
                        FROM ClazzAssignmentRollUp
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid), 0)
        """

        private const val GET_TOTAL_COMPLETE_CONTENT_OF_ASSIGNMENT_FOR_USER = """
             COALESCE((SELECT COUNT(cacheContentComplete)
                         FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                          AND cacheContentComplete
                          AND cachePersonUid = :accountPersonUid), 0)
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