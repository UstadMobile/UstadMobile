package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.SqliteOnly
import com.ustadmobile.lib.db.entities.ClazzAssignmentRollUp
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress

@Dao
@Repository
abstract class ClazzAssignmentRollUpDao: BaseDao<ClazzAssignmentRollUp> {

    @Query(""" 
        REPLACE INTO ClazzAssignmentRollUp 
                (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid, 
                 cacheStudentScore, cacheMaxScore, cacheWeight,  cacheProgress,
                 cacheContentComplete, cacheSuccess,cachePenalty, cacheFinalWeightScoreWithPenalty, lastCsnChecked)
                 
        WITH MaxScoreTable (maxScore, maxScoreContentEntryUid) 
                AS (SELECT MAX(resultScoreMax), statementContentEntryUid 
                      FROM StatementEntity
                     WHERE contentEntryRoot 
                  GROUP BY statementContentEntryUid)               

       SELECT clazzEnrolmentPersonUid AS cachePersonUid, 
                COALESCE(cacjContentUid,0) AS cacheContentEntryUid, caUid AS cacheClazzAssignmentUid, 
               COALESCE(resultScoreRaw,0) AS cacheStudentScore, 
              
              
               COALESCE((SELECT maxScore 
                          FROM MaxScoreTable 
                         WHERE cacjContentUid = maxScoreContentEntryUid), 0) AS cacheMaxScore,
                         
               COALESCE(cacjWeight, 0) AS cacheWeight,
                        
                          
               COALESCE(StatementEntity.extensionProgress,0) AS cacheProgress,
               COALESCE(StatementEntity.resultCompletion,'FALSE') AS cacheContentComplete, 
               COALESCE(StatementEntity.resultSuccess,0) AS cacheSuccess,
               (CASE WHEN StatementEntity.timestamp > ClazzAssignment.caDeadlineDate 
                     THEN ClazzAssignment.caLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
                     
              (CASE WHEN StatementEntity.timestamp > ClazzAssignment.caDeadlineDate 
                     THEN (COALESCE(CAST(resultScoreRaw AS REAL),0) / COALESCE((SELECT maxScore 
                          FROM MaxScoreTable WHERE cacjContentUid = maxScoreContentEntryUid),0) * 100 * cacjWeight * (1 - (CAST(caLateSubmissionPenalty AS REAL)/100)))
                     ELSE (COALESCE(CAST(resultScoreRaw AS REAL),0) / COALESCE((SELECT maxScore 
                          FROM MaxScoreTable WHERE cacjContentUid = maxScoreContentEntryUid),0) * 100 * cacjWeight)  END) AS cacheFinalWeightScoreWithPenalty,   
                     
               0 AS lastCsnChecked
          FROM ClazzAssignmentContentJoin
	            JOIN ClazzAssignment 
                ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                
                JOIN ClazzEnrolment
                ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
					      	      
			    LEFT JOIN StatementEntity 
	            ON statementUid = (SELECT statementUid 
                                     FROM StatementEntity 
                                            LEFT JOIN ClazzAssignment 
                                            ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid 
                                    WHERE StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                                      AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                      AND StatementEntity.contentEntryRoot  
                                      AND StatementEntity.timestamp 
                                            BETWEEN ClazzAssignment.caStartDate
                                            AND ClazzAssignment.caGracePeriodDate
                                  ORDER BY CASE WHEN StatementEntity.timestamp > ClazzAssignment.caDeadlineDate 
                                                THEN StatementEntity.resultScoreScaled * (1 - (CAST(caLateSubmissionPenalty AS REAL)/100))
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
         UNION 
         SELECT clazzEnrolmentPersonUid AS cachePersonUid, 
                0 AS cacheContentEntryUid, 
                caUid AS cacheClazzAssignmentUid, 
                COALESCE(MarkingStatement.resultScoreRaw,0) AS cacheStudentScore, 
                COALESCE(caMaxScore,0) AS cacheMaxScore,
                COALESCE(caFileSubmissionWeight, 0) AS cacheWeight,
                
                COALESCE(MarkingStatement.extensionProgress,0) AS cacheProgress,
                COALESCE(MarkingStatement.resultCompletion,'FALSE') AS cacheContentComplete, 
                COALESCE(MarkingStatement.resultSuccess,0) AS cacheSuccess,
                (CASE WHEN SubmissionStatement.timestamp > ClazzAssignment.caDeadlineDate 
                     THEN ClazzAssignment.caLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
                     
              (CASE WHEN SubmissionStatement.timestamp > ClazzAssignment.caDeadlineDate 
                     THEN (COALESCE(CAST(MarkingStatement.resultScoreRaw AS REAL),0) / COALESCE(caMaxScore,0) * 
                            100 * caFileSubmissionWeight * (1 - (CAST(caLateSubmissionPenalty AS REAL)/100)))
                     ELSE (COALESCE(CAST(MarkingStatement.resultScoreRaw AS REAL),0) / COALESCE(caMaxScore,0) * 
                            100 * caFileSubmissionWeight)  END) AS cacheFinalWeightScoreWithPenalty, 
                     
                   
               0 AS lastCsnChecked
         FROM ClazzAssignment
              JOIN ClazzEnrolment
              ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
              
              LEFT JOIN StatementEntity AS SubmissionStatement
	          ON SubmissionStatement.statementUid = (SELECT statementUid 
                                   FROM StatementEntity
                                  WHERE StatementEntity.statementContentEntryUid = 0
                                    AND xObjectUid = ClazzAssignment.caXObjectUid
                                    AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                    AND StatementEntity.timestamp 
                                        BETWEEN ClazzAssignment.caStartDate
                                        AND ClazzAssignment.caGracePeriodDate
                               ORDER BY timestamp DESC LIMIT 1
                                  )
              LEFT JOIN XObjectEntity AS ObjectStatementRef
              ON ObjectStatementRef.objectStatementRefUid = SubmissionStatement.statementUid                    
                                  
              LEFT JOIN StatementEntity AS MarkingStatement
              ON MarkingStatement.xObjectUid = ObjectStatementRef.xObjectUid
              
        WHERE ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
          AND ClazzEnrolment.clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
          AND ClazzEnrolment.clazzEnrolmentActive
          AND ClazzAssignment.caActive
          AND ClazzAssignment.caRequireFileSubmission
          AND (:clazzUid = 0 OR ClazzAssignment.caClazzUid = :clazzUid)
          AND (:assignmentUid = 0 OR ClazzAssignment.caUid = :assignmentUid)
          AND (:personUid = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :personUid)
      GROUP BY cacheClazzAssignmentUid, cacheContentEntryUid, cachePersonUid     
    """)
    @SqliteOnly
    abstract suspend fun cacheBestStatements(clazzUid: Long, assignmentUid: Long, personUid: Long)


    @Query("""
        SELECT COALESCE(ClazzAssignmentRollUp.cacheMaxScore,0) AS resultMax, 
               COALESCE(ClazzAssignmentRollUp.cacheStudentScore,0) AS resultScore, 
               0 as resultScaled,
               COALESCE(ClazzAssignmentRollUp.cacheContentComplete,'FALSE') AS contentComplete,
               COALESCE(AVG(cacheProgress),0) as progress, 0 as success,
               COALESCE(ClazzAssignmentRollUp.cachePenalty,0) AS penalty,
               
               COALESCE(SUM(cacheWeight),0) As resultWeight,
               
              COALESCE((CASE WHEN ClazzAssignmentRollUp.cacheContentComplete 
                                            THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                        
               1 AS totalContent
 
     	  FROM ClazzAssignmentRollUp
         WHERE cachePersonUid = :personUid
           AND cacheClazzAssignmentUid = :assignmentUid
           AND cacheContentEntryUid = 0
    """)
    abstract fun getScoreForFileSubmission(assignmentUid: Long, personUid: Long): DoorLiveData<ContentEntryStatementScoreProgress?>

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