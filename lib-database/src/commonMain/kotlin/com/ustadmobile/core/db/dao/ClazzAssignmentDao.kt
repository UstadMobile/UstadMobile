package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_ACTIVE_DESC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDao.Companion.SORT_LAST_NAME_DESC
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.*


@Dao
@Repository
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

    @Query("""
        WITH CtePermissionCheck (hasPermission) 
          AS (SELECT EXISTS(SELECT 1 
                FROM Clazz 
                WHERE Clazz.clazzUid = :clazzUid 
                AND :accountPersonUid IN (${ClazzDao.ENTITY_PERSONS_WITH_PERMISSION})))
                
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
                                       WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid)
                                  AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid))
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
                           AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid) = 
                                    (SELECT COUNT(ClazzAssignmentContentJoin.cacjContentUid) 
                                       FROM ClazzAssignmentContentJoin 
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid)) 
                  ELSE 0 END) AS completedStudents, 
           
            COALESCE((SELECT SUM(cacheStudentScore) 
                        FROM CacheClazzAssignment
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid),0) AS resultScore,
                          
                          
            COALESCE((SELECT SUM(cacheMaxScore) 
                        FROM CacheClazzAssignment 
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid),0) AS resultMax,
                                
                                
            COALESCE((SELECT COUNT(cacheContentComplete) = COUNT(cacheContentEntryUid) 
                        FROM CacheClazzAssignment
                        WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                          AND cachePersonUid = :accountPersonUid),'FALSE') AS contentComplete,
                          
             0 as success,           
                 
              
              0 as progress
             
             FROM ClazzAssignment
            WHERE caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
              AND (hasMetricsPermission OR :timestamp >= ClazzAssignment.caStartDate)
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_START_DATE_ASC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_ASC THEN (resultScore/resultMax)
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_START_DATE_DESC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_DESC THEN (resultScore/resultMax)
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
    abstract fun getAllAssignments(clazzUid: Long, timestamp: Long, accountPersonUid: Long,
                                   sortOrder: Int, searchText: String, permission: Long)
            : DataSource.Factory<Int, ClazzAssignmentWithMetrics>


    @Query("""
        SELECT COALESCE(SUM(ResultSource.cacheMaxScore),0) AS resultMax, 
               COALESCE(SUM(ResultSource.cacheStudentScore),0) AS resultScore, 
               'FALSE' as contentComplete, 0 as progress, 0 as success
     	  FROM (SELECT CacheClazzAssignment.cacheStudentScore, CacheClazzAssignment.cacheMaxScore
     	 	      FROM ClazzAssignmentContentJoin 
                         LEFT JOIN ContentEntry 
                         ON ContentEntry.contentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                         
                         LEFT JOIN CacheClazzAssignment
                         ON cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                         AND cachePersonUid = :personUid
                         AND cacheClazzAssignmentUid = :caUid
                  GROUP BY ClazzAssignmentContentJoin.cacjContentUid
     	  ) AS ResultSource
    """)
    abstract suspend fun getStatementScoreProgressForAssignment(caUid: Long, personUid: Long): ContentEntryStatementScoreProgress?


    @Query("""
         SELECT ResultSource.personUid, ResultSource.firstNames, ResultSource.lastName,
            COUNT(DISTINCT(ResultSource.contextRegistration)) AS attempts, 
            MIN(ResultSource.timestamp) AS startDate, 
            MAX(ResultSource.timestamp) AS endDate, 
            SUM(ResultSource.resultDuration) AS duration, 
            
            
             (SELECT AVG(cacheProgress) 
               FROM CacheClazzAssignment 
              WHERE cacheClazzAssignmentUid = :assignmentUid
                AND cachePersonUid = ResultSource.personUid
                ) AS progress,
                
               ((CAST((SELECT SUM(cacheStudentScore) 
                         FROM CacheClazzAssignment 
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                          AND cachePersonUid =  ResultSource.personUid) AS REAL) 
                          / 
                          (SELECT SUM(cacheMaxScore)
                             FROM CacheClazzAssignment 
                            WHERE cacheClazzAssignmentUid = :assignmentUid
                              AND cachePersonUid = ResultSource.personUid)) * 100) AS score,
                              
                              0 as success,
            
            cm.commentsText AS latestPrivateComment
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            StatementEntity.contextRegistration, StatementEntity.timestamp, 
            StatementEntity.resultDuration
        
         ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.FROM_PERSONGROUPMEMBER_JOIN_PERSON_WITH_PERMISSION_PT2}
             LEFT JOIN StatementEntity 
                ON StatementEntity.statementPersonUid = Person.personUid 
                    WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                        AND statementContentEntryUid 
                            IN (SELECT cacjContentUid
                                  FROM ClazzAssignmentContentJoin
                                        JOIN ClazzAssignment 
                                        ON ClazzAssignment.caUid = cacjAssignmentUid
                                        
                                        JOIN ClazzEnrolment
                                        ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                                        AND ClazzEnrolment.clazzEnrolmentPersonUid = StatementEntity.statementPersonUid
                                 WHERE cacjAssignmentUid = :assignmentUid
                                  AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                                  AND ClazzEnrolment.clazzEnrolmentActive
                                  AND StatementEntity.timestamp
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate)
                        AND PersonGroupMember.groupMemberActive  
                        AND Person.firstNames || ' ' || Person.lastName LIKE :searchText 
             GROUP BY StatementEntity.statementUid) AS ResultSource 
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
    abstract fun getAttemptSummaryForStudentsInAssignment(assignmentUid: Long, accountPersonUid: Long,
                                                          searchText: String, sortOrder: Int):
            DataSource.Factory<Int, PersonWithAttemptsSummary>

    @Query("""
        WITH CtePermissionCheck (hasPermission) 
          AS (SELECT EXISTS(SELECT 1 
                FROM Clazz 
                WHERE Clazz.clazzUid = :clazzUid 
                AND :accountPersonUid IN (${ClazzDao.ENTITY_PERSONS_WITH_PERMISSION})))
                
                
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
                                       WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid)
                                  AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid))
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
                           AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid) = 
                                    (SELECT COUNT(ClazzAssignmentContentJoin.cacjContentUid) 
                                       FROM ClazzAssignmentContentJoin 
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid)) 
                  ELSE 0 END) AS completedStudents

        
        FROM ClazzAssignment
       WHERE caActive
         AND caClazzUid = :clazzUid 
         AND caUid = :uid
    """)
    abstract suspend fun getStudentsProgressOnAssignment(clazzUid: Long, accountPersonUid: Long,
                                                 uid: Long, permission: Long): StudentAssignmentProgress?



    @Update
    abstract suspend fun updateAsync(clazzAssignment: ClazzAssignment)

    @Query("""
        SELECT * 
          FROM ClazzAssignment 
         WHERE caUid = :uid
    """)
    abstract suspend fun findByUidAsync(uid: Long): ClazzAssignment?

    companion object{

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