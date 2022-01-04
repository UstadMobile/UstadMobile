package com.ustadmobile.core.db.dao

import com.ustadmobile.door.DoorDataSourceFactory
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_DESC
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("""
     REPLACE INTO ClazzAssignmentReplicate(caPk, caDestination)
      SELECT ClazzAssignment.caUid AS caUid,
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
  SELECT ClazzAssignment.caUid AS caUid,
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
        WITH CtePermissionCheck (hasPermission) 
            AS (SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          :permission
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid))           
                
        SELECT ClazzAssignment.*, 
        
           (SELECT hasPermission FROM CtePermissionCheck) AS hasMetricsPermission,
            (SELECT COUNT(*) 
                        FROM ClazzEnrolment 
                        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid 
                        AND ClazzEnrolment.clazzEnrolmentActive 
                        AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                        AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft) 
                        AS totalStudents, 
        
            (CASE WHEN (SELECT hasPermission 
                          FROM CtePermissionCheck)
                 THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid)
                         FROM ClazzEnrolment
                         
                        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                          AND ClazzEnrolment.clazzEnrolmentActive
                          AND ClazzAssignment.caClazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                          AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft 
                          AND NOT EXISTS 
                              (SELECT statementUid 
                                 FROM StatementEntity 
                                WHERE statementContentEntryUid 
                                   IN (SELECT cacjContentUid 
                                        FROM ClazzAssignmentContentJoin 
                                       WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                         AND cacjActive)
                                  AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                  AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate
                                  ))
                ELSE 0 END) AS notStartedStudents,
                
                  0 as startedStudents,
        
            (CASE WHEN (SELECT hasPermission 
                         FROM CtePermissionCheck)
                  THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid) 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                           AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                           AND ClazzEnrolment.clazzEnrolmentActive
                           AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft 
                           AND (SELECT COUNT(DISTINCT statementContentEntryUid)
                                  FROM StatementEntity
                                 WHERE statementContentEntryUid 
                                    IN (SELECT cacjContentUid 
                                          FROM ClazzAssignmentContentJoin 
                                         WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid)
                           AND StatementEntity.contentEntryRoot 
                           AND StatementEntity.resultCompletion
                           AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate
                           AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid) = 
                                    (SELECT COUNT(ClazzAssignmentContentJoin.cacjContentUid) 
                                       FROM ClazzAssignmentContentJoin 
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid)) 
                  ELSE 0 END) AS completedStudents, 
           
            $GET_ALL_ASSIGNMENTS_SCORE_FOR_CURRENT_USER_SQL AS resultScore,
                          
            $GET_ALL_ASSIGNMENTS_GET_MAX_SCORE_FOR_CURRENT_USER AS resultMax,
                                
            COALESCE((SELECT COUNT(cacheContentComplete)
                        FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                        AND cacheContentComplete
                              AND cachePersonUid = :accountPersonUid)  =  
                          
                          (SELECT COUNT(DISTINCT cacheContentEntryUid) 
                             FROM ClazzAssignmentRollUp
                            WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid), 'FALSE') AS contentComplete,
                          
            COALESCE((SELECT AVG(cachePenalty) 
                        FROM ClazzAssignmentRollUp 
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid),0) AS penalty,
                                               
            COALESCE((SELECT COUNT(cacheContentComplete)
                        FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                        AND cacheContentComplete
                              AND cachePersonUid = :accountPersonUid), 0) AS totalCompletedContent,
            
            COALESCE((SELECT COUNT(DISTINCT cacheContentEntryUid) 
                              FROM ClazzAssignmentRollUp
                             WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid), 0) AS totalContent,
                            
                                               
                          
             0 as success,           
             0 as resultScaled,    
              
              0 as progress
             
             FROM ClazzAssignment
            WHERE ClazzAssignment.caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
              AND ((SELECT hasPermission FROM CtePermissionCheck) OR :timestamp >= ClazzAssignment.caStartDate)
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_START_DATE_ASC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_ASC THEN ($GET_ALL_ASSIGNMENTS_SCORE_FOR_CURRENT_USER_SQL/$GET_ALL_ASSIGNMENTS_GET_MAX_SCORE_FOR_CURRENT_USER)
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_START_DATE_DESC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_DESC THEN ($GET_ALL_ASSIGNMENTS_SCORE_FOR_CURRENT_USER_SQL/$GET_ALL_ASSIGNMENTS_GET_MAX_SCORE_FOR_CURRENT_USER)
                ELSE 0
            END DESC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_ASC THEN ClazzAssignment.caTitle
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_TITLE_DESC THEN ClazzAssignment.caTitle
                ELSE ''
            END DESC
    """)
    @QueryLiveTables(["ClazzAssignment", "ScopedGrant", "ClazzAssignmentRollUp",
        "PersonGroupMember", "ClazzEnrolment"])
    abstract fun getAllAssignments(clazzUid: Long, timestamp: Long, accountPersonUid: Long,
                                   sortOrder: Int, searchText: String, permission: Long)
            : DoorDataSourceFactory<Int, ClazzAssignmentWithMetrics>


    @Query("""
        SELECT COALESCE(SUM(ResultSource.cacheMaxScore),0) AS resultMax, 
               COALESCE(SUM(ResultSource.cacheStudentScore),0) AS resultScore, 
               0 as resultScaled,
               'FALSE' as contentComplete, 0 as progress, 0 as success,
               COALESCE(AVG(ResultSource.cachePenalty),0) AS penalty,
               
              COALESCE((SUM(CASE 
                        WHEN CAST(ResultSource.cacheContentComplete AS INTEGER) > 0 
                        THEN 1 ELSE 0 END)), 0) AS totalCompletedContent,
                        
               COALESCE(COUNT(DISTINCT ResultSource.cacheContentEntryUid), 0) AS totalContent
 
     	  FROM (SELECT ClazzAssignmentRollUp.cacheStudentScore, ClazzAssignmentRollUp.cacheMaxScore,
                        ClazzAssignmentRollUp.cachePenalty, ClazzAssignmentRollUp.cacheContentComplete,
                        ClazzAssignmentRollUp.cacheContentEntryUid
     	 	      FROM ClazzAssignmentContentJoin 
                         LEFT JOIN ClazzAssignmentRollUp
                         ON ClazzAssignmentRollUp.cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                         AND ClazzAssignmentRollUp.cachePersonUid = :personUid
                         AND ClazzAssignmentRollUp.cacheClazzAssignmentUid = :caUid
                  WHERE ClazzAssignmentContentJoin.cacjActive
                  GROUP BY ClazzAssignmentContentJoin.cacjContentUid
     	  ) AS ResultSource
    """)
    abstract suspend fun getStatementScoreProgressForAssignment(caUid: Long, personUid: Long): ContentEntryStatementScoreProgress?
    
    @Query("""
         SELECT ResultSource.personUid, ResultSource.firstNames, ResultSource.lastName,
            COALESCE(COUNT(DISTINCT(ResultSource.contextRegistration)),0) AS attempts, 
            COALESCE(MIN(ResultSource.timestamp),0) AS startDate, 
            COALESCE(MAX(ResultSource.timestamp),0) AS endDate, 
            SUM(ResultSource.resultDuration) AS duration, 
            
            
             (SELECT AVG(cacheProgress) 
               FROM ClazzAssignmentRollUp 
              WHERE cacheClazzAssignmentUid = :assignmentUid
                AND cachePersonUid = ResultSource.personUid
                ) AS progress,
                
                0 as success, 
                0 as resultScaled,
                
                'FALSE' as contentComplete,
                
                (SELECT SUM(cacheStudentScore) 
                         FROM ClazzAssignmentRollUp 
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                          AND cachePersonUid =  ResultSource.personUid) AS resultScore,
                          
                (SELECT SUM(cacheMaxScore)
                             FROM ClazzAssignmentRollUp 
                            WHERE cacheClazzAssignmentUid = :assignmentUid
                              AND cachePersonUid = ResultSource.personUid) AS resultMax, 
                                        
                 (SELECT AVG(cachePenalty)
                             FROM ClazzAssignmentRollUp 
                            WHERE cacheClazzAssignmentUid = :assignmentUid
                              AND cachePersonUid = ResultSource.personUid) AS penalty,   
                                                    
                   COALESCE((SELECT COUNT(cacheContentComplete)
                        FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                        AND cacheContentComplete
                              AND cachePersonUid = ResultSource.personUid), 0) AS totalCompletedContent,
            
            COALESCE((SELECT COUNT(DISTINCT cacheContentEntryUid) 
                              FROM ClazzAssignmentRollUp
                             WHERE cacheClazzAssignmentUid = :assignmentUid), 0) AS totalContent,                              
                                                    

            cm.commentsText AS latestPrivateComment
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            StatementEntity.contextRegistration, StatementEntity.timestamp, 
            StatementEntity.resultDuration 
                FROM PersonGroupMember
         ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             LEFT JOIN ClazzEnrolment
             ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid 
                AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
            
             LEFT JOIN ClazzAssignment 
             ON ClazzAssignment.caUid = :assignmentUid
             
             LEFT JOIN ClazzAssignmentContentJoin 
             ON ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid 
		                   
                          
             LEFT JOIN StatementEntity 
             ON StatementEntity.statementPersonUid = Person.personUid  
                AND StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid  
                AND StatementEntity.timestamp
                BETWEEN ClazzAssignment.caStartDate
                AND ClazzAssignment.caGracePeriodDate    
               
                WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                AND PersonGroupMember.groupMemberActive                      
                AND ClazzAssignmentContentJoin.cacjActive
                AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
                AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}     
                AND ClazzEnrolment.clazzEnrolmentActive
                GROUP BY Person.personUid, StatementEntity.statementUid) AS ResultSource 
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
        
            (CASE WHEN (SELECT hasPermission 
                          FROM CtePermissionCheck)
                 THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid)
                         FROM ClazzEnrolment
                         
                        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                          AND ClazzEnrolment.clazzEnrolmentActive
                          AND ClazzAssignment.caClazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                          AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft 
                          AND NOT EXISTS 
                              (SELECT statementUid 
                                 FROM StatementEntity 
                                WHERE statementContentEntryUid 
                                   IN (SELECT cacjContentUid 
                                        FROM ClazzAssignmentContentJoin 
                                       WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                         AND ClazzAssignmentContentJoin.cacjActive)
                                  AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                   AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate
                                  ))
                ELSE 0 END) AS notStartedStudents,
                
                0 as startedStudents,
        
            (CASE WHEN (SELECT hasPermission 
                         FROM CtePermissionCheck)
                  THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid) 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                           AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                           AND ClazzEnrolment.clazzEnrolmentActive
                           AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft 
                           AND (SELECT COUNT(DISTINCT statementContentEntryUid)
                                  FROM StatementEntity
                                 WHERE statementContentEntryUid 
                                    IN (SELECT cacjContentUid 
                                          FROM ClazzAssignmentContentJoin 
                                         WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                           AND ClazzAssignmentContentJoin.cacjActive)
                           AND StatementEntity.contentEntryRoot 
                           AND StatementEntity.resultCompletion
                           AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate
                           AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid) = 
                                    (SELECT COUNT(ClazzAssignmentContentJoin.cacjContentUid) 
                                       FROM ClazzAssignmentContentJoin 
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid
                                        AND cacjActive)) 
                  ELSE 0 END) AS completedStudents

        
        FROM ClazzAssignment
       WHERE caActive
         AND caClazzUid = :clazzUid 
         AND caUid = :uid
    """)
    abstract fun getStudentsProgressOnAssignment(clazzUid: Long, accountPersonUid: Long,
                                                 uid: Long, permission: Long): DoorLiveData<AssignmentProgressSummary?>



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

        private const val GET_ALL_ASSIGNMENTS_SCORE_FOR_CURRENT_USER_SQL = """
        COALESCE((SELECT MAX(cacheStudentScore) 
                        FROM ClazzAssignmentRollUp
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid),0)
        """

        private const val GET_ALL_ASSIGNMENTS_GET_MAX_SCORE_FOR_CURRENT_USER = """
            COALESCE((SELECT MAX(cacheMaxScore) 
                        FROM ClazzAssignmentRollUp 
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid),0)
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