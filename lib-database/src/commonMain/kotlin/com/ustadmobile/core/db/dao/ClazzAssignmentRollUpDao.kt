package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.SqliteOnly
import com.ustadmobile.lib.db.entities.ClazzAssignmentRollUp
import com.ustadmobile.lib.db.entities.ClazzEnrolment

@Dao
@Repository
abstract class ClazzAssignmentRollUpDao: BaseDao<ClazzAssignmentRollUp> {

    @Query(""" 
        REPLACE INTO ClazzAssignmentRollUp 
                (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid, 
                 cacheStudentScore, cacheMaxScore, cacheProgress, 
                 cacheContentComplete, cacheSuccess,cachePenalty, lastCsnChecked)
                 
        WITH MaxScoreTable (maxScore, maxScoreContentEntryUid) 
                AS (SELECT MAX(resultScoreMax), statementContentEntryUid 
                      FROM StatementEntity
                     WHERE contentEntryRoot 
                  GROUP BY statementContentEntryUid)               

       SELECT clazzEnrolmentPersonUid AS cachePersonUid, 
                cacjContentUid AS cacheContentEntryUid, caUid AS cacheClazzAssignmentUid, 
               COALESCE(resultScoreRaw,0) AS cacheStudentScore, 
               
               COALESCE((SELECT maxScore 
                          FROM MaxScoreTable 
                         WHERE cacjContentUid = maxScoreContentEntryUid), 0) AS cacheMaxScore,
                          
               COALESCE(StatementEntity.extensionProgress,0) AS cacheProgress,
               COALESCE(StatementEntity.resultCompletion,'FALSE') AS cacheContentComplete, 
               COALESCE(StatementEntity.resultSuccess,0) AS cacheSuccess,
               (CASE WHEN StatementEntity.timestamp > ClazzAssignment.caDeadlineDate 
                     THEN ClazzAssignment.caLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
               COALESCE((SELECT MAX(statementLocalChangeSeqNum) FROM StatementEntity),0) AS lastCsnChecked
          FROM ClazzAssignmentContentJoin
	            LEFT JOIN ClazzAssignment 
                ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                
                LEFT JOIN ClazzEnrolment
                ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
					      	      
			    LEFT JOIN StatementEntity 
	            ON statementUid = (SELECT statementUid 
                                     FROM StatementEntity 
                                            LEFT JOIN ClazzAssignment 
                                            ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid 
                                    WHERE StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                                      AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                      AND StatementEntity.contentEntryRoot 
                                      AND StatementEntity.statementLocalChangeSeqNum >= 
                                                COALESCE((SELECT MAX(lastCsnChecked) 
                                                            FROM ClazzAssignmentRollUp),0)
                                      AND StatementEntity.timestamp 
                                            BETWEEN ClazzAssignment.caStartDate
                                            AND ClazzAssignment.caGracePeriodDate
                                  ORDER BY CASE WHEN StatementEntity.timestamp > ClazzAssignment.caDeadlineDate 
                                                THEN StatementEntity.resultScoreScaled * (1 - (caLateSubmissionPenalty/100))
                                                ELSE StatementEntity.resultScoreScaled END DESC, 
                                            StatementEntity.extensionProgress DESC, 
                                            StatementEntity.resultSuccess DESC LIMIT 1)
                LEFT JOIN ClazzAssignmentRollUp
                ON ClazzAssignmentRollUp.cacheContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid 
                AND ClazzAssignmentRollUp.cachePersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                AND ClazzAssignmentRollUp.cacheClazzAssignmentUid = ClazzAssignment.caUid
                                            
                                            
	     WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
           AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
           AND ClazzEnrolment.clazzEnrolmentActive
           AND ClazzAssignment.caActive
           AND ClazzAssignmentContentJoin.cacjActive
           AND (:clazzUid = 0 OR ClazzAssignment.caClazzUid = :clazzUid)
           AND (:assignmentUid = 0 OR ClazzAssignment.caUid = :assignmentUid)
           AND (:personUid = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :personUid)
           AND (COALESCE(StatementEntity.resultScoreRaw,0) >= COALESCE(ClazzAssignmentRollUp.cacheStudentScore,0)
                    AND COALESCE(StatementEntity.extensionProgress,0) >= COALESCE(ClazzAssignmentRollUp.cacheProgress,0)
                    AND COALESCE(StatementEntity.resultSuccess,0) >= COALESCE(ClazzAssignmentRollUp.cacheSuccess,0))
      GROUP BY cacheClazzAssignmentUid, cacheContentEntryUid, cachePersonUid
    """)
    @SqliteOnly
    abstract suspend fun cacheBestStatements(clazzUid: Long, assignmentUid: Long, personUid: Long)

    @Query("""
        DELETE
         FROM ClazzAssignmentRollUp
        WHERE cacheContentEntryUid 
                IN (SELECT cacjContentUid 
                     FROM ClazzAssignmentContentJoin
                    WHERE NOT cacjActive)
    """)
    abstract suspend fun deleteCachedInactiveContent()


    @Query("""
        UPDATE ClazzAssignmentRollUp 
           SET lastCsnChecked = 0
         WHERE cacheClazzAssignmentUid = :changedAssignmentUid
    """)
    abstract suspend fun invalidateCacheByAssignment(changedAssignmentUid: Long)

    @Query("""
        UPDATE ClazzAssignmentRollUp 
           SET lastCsnChecked = 0
         WHERE cacheClazzAssignmentUid IN (:changedAssignmentUid)
    """)
    abstract suspend fun invalidateCacheByAssignmentList(changedAssignmentUid: List<Long>)

}