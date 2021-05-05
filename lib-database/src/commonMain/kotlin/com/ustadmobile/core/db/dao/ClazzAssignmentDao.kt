package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
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
                        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                        AND ClazzEnrolment.clazzEnrolmentActive
                        AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}) 
                        AS totalStudents, 
        
            (CASE WHEN (SELECT hasPermission 
                          FROM CtePermissionCheck)
                 THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid)
                         FROM ClazzEnrolment
                         
                        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                          AND ClazzEnrolment.clazzEnrolmentActive
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
        
            (CASE WHEN (SELECT hasPermission 
                         FROM CtePermissionCheck)
                  THEN (SELECT COUNT(DISTINCT clazzEnrolmentPersonUid) 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
                           AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft 
                           AND (SELECT COUNT(DISTINCT statementContentEntryUid)
                                  FROM StatementEntity
                                 WHERE statementContentEntryUid 
                                    IN (SELECT cacjContentUid 
                                          FROM ClazzAssignmentContentJoin 
                                         WHERE ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid)
                           AND StatementEntity.contentEntryRoot 
                           AND StatementEntity.statementVerbUid 
                                IN (${VerbEntity.VERB_COMPLETED_UID}, ${VerbEntity.VERB_SATISFIED_UID}, 
                                ${VerbEntity.VERB_PASSED_UID}, ${VerbEntity.VERB_FAILED_UID})
                           AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid) = 
                                    (SELECT COUNT(ClazzAssignmentContentJoin.cacjContentUid) 
                                       FROM ClazzAssignmentContentJoin 
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid)) 
                  ELSE 0 END) AS completedStudents, 
                  Source.resultScore, Source.resultMax, Source.contentComplete, Source.progress
        
        
        
            
             FROM ClazzAssignment
                    LEFT JOIN (SELECT COALESCE(SUM(ResultSource.resultScoreMax),0) AS resultMax, 
                                      COALESCE(SUM(ResultSource.resultScoreRaw),0) AS resultScore, 
                                       COALESCE(COUNT(ResultSource.resultCompletion) = COUNT(ResultSource.cacjContentUid),'FALSE') as contentComplete, 
                                       0 as progress, ResultSource.cacjAssignmentUid AS assignmentUid
     	                         FROM (SELECT StatementEntity.resultScoreRaw, 
                                               StatementEntity.resultScoreMax, 
                                               StatementEntity.resultCompletion,
                                               ClazzAssignmentContentJoin.cacjAssignmentUid,
                                               ClazzAssignmentContentJoin.cacjContentUid
     	 	                            FROM ClazzAssignmentContentJoin 
                                             LEFT JOIN ContentEntry 
                                             ON ContentEntry.contentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                       
                                             LEFT JOIN StatementEntity 
                                             ON statementUid = (SELECT statementUid 
                                                                  FROM StatementEntity 
                                                                        LEFT JOIN ClazzAssignment 
                                                                        ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                                                 WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
                                                                   AND (:role = ${ClazzEnrolment.ROLE_STUDENT} AND statementPersonUid = :accountPersonUid)
                                                                   AND contentEntryRoot 
                                                                   AND StatementEntity.timestamp 
                                                                        BETWEEN ClazzAssignment.caStartDate
                                                                            AND ClazzAssignment.caGracePeriodDate
                                                             ORDER BY resultScoreScaled DESC LIMIT 1)
     	  ) AS ResultSource) AS Source ON ClazzAssignment.caUid = Source.assignmentUid
                    JOIN Clazz ON Clazz.clazzUid = :clazzUid
            WHERE caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
              AND :timestamp >= ClazzAssignment.caStartDate
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
                                   sortOrder: Int, searchText: String, role: Int, permission: Long)
            : DataSource.Factory<Int, ClazzAssignmentWithMetrics>


    @Query("""
        SELECT COALESCE(SUM(ResultSource.resultScoreMax),0) AS resultMax, 
               COALESCE(SUM(ResultSource.resultScoreRaw),0) AS resultScore, 
               'FALSE' as contentComplete, 0 as progress
     	  FROM (SELECT StatementEntity.resultScoreRaw, StatementEntity.resultScoreMax
     	 	      FROM ClazzAssignmentContentJoin 
                         LEFT JOIN ContentEntry 
                         ON ContentEntry.contentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                       
                         LEFT JOIN StatementEntity 
                         ON statementUid = (SELECT statementUid 
                                              FROM StatementEntity 
                                                    LEFT JOIN ClazzAssignment 
                                                    ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                             WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
                                               AND caUid = :caUid
                                               AND statementPersonUid = :personUid
                                               AND contentEntryRoot 
                                               AND StatementEntity.timestamp 
                                                    BETWEEN ClazzAssignment.caStartDate
                                                        AND ClazzAssignment.caGracePeriodDate
                                         ORDER BY resultScoreScaled DESC LIMIT 1)
     	  ) AS ResultSource
    """)
    abstract suspend fun getStatementScoreProgressForAssignment(caUid: Long, personUid: Long): ContentEntryStatementScoreProgress?


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