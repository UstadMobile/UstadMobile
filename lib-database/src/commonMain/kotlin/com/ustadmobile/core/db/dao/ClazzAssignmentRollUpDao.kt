package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.SqliteOnly
import com.ustadmobile.lib.db.entities.ClazzAssignmentRollUp
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.CourseBlock

@DoorDao
@Repository
expect abstract class ClazzAssignmentRollUpDao: BaseDao<ClazzAssignmentRollUp> {

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
               (CASE WHEN StatementEntity.timestamp > CourseBlock.cbDeadlineDate
                     THEN CourseBlock.cbLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
                     
              (CASE WHEN StatementEntity.timestamp > CourseBlock.cbDeadlineDate 
                     THEN (COALESCE(CAST(resultScoreRaw AS REAL),0) / COALESCE((SELECT maxScore 
                          FROM MaxScoreTable WHERE cacjContentUid = maxScoreContentEntryUid),0) * 100 * cacjWeight * (1 - (CAST(cbLateSubmissionPenalty AS REAL)/100)))
                     ELSE (COALESCE(CAST(resultScoreRaw AS REAL),0) / COALESCE((SELECT maxScore 
                          FROM MaxScoreTable WHERE cacjContentUid = maxScoreContentEntryUid),0) * 100 * cacjWeight)  END) AS cacheFinalWeightScoreWithPenalty,   
                     
               0 AS lastCsnChecked
          FROM ClazzAssignmentContentJoin
	            JOIN ClazzAssignment 
                ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                
                JOIN ClazzEnrolment
                ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                
                JOIN CourseBlock
                ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE} 
					      	      
			    LEFT JOIN StatementEntity 
	            ON statementUid = (SELECT statementUid 
                                     FROM StatementEntity 
                                            LEFT JOIN ClazzAssignment 
                                            ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid 
                                              JOIN CourseBlock
                                                ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
                                               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE} 
                                    WHERE StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                                      AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                      AND StatementEntity.contentEntryRoot  
                                      AND StatementEntity.timestamp 
                                            BETWEEN CourseBlock.cbHideUntilDate
                                            AND CourseBlock.cbGracePeriodDate
                                  ORDER BY CASE WHEN StatementEntity.timestamp > CourseBlock.cbDeadlineDate 
                                                THEN StatementEntity.resultScoreScaled * (1 - (CAST(CourseBlock.cbLateSubmissionPenalty AS REAL)/100))
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
                COALESCE(cbMaxPoints,0) AS cacheMaxScore,
                0 AS cacheWeight,
                
                COALESCE(MarkingStatement.extensionProgress,0) AS cacheProgress,
                COALESCE(MarkingStatement.resultCompletion,'FALSE') AS cacheContentComplete, 
                COALESCE(MarkingStatement.resultSuccess,0) AS cacheSuccess,
                (CASE WHEN SubmissionStatement.timestamp > CourseBlock.cbDeadlineDate 
                     THEN CourseBlock.cbLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
                     
              (CASE WHEN SubmissionStatement.timestamp > CourseBlock.cbDeadlineDate 
                     THEN (COALESCE(CAST(MarkingStatement.resultScoreRaw AS REAL),0) / COALESCE(CourseBlock.cbMaxPoints,0) * 
                            100 * (1 - (CAST(cbLateSubmissionPenalty AS REAL)/100)))
                     ELSE (COALESCE(CAST(MarkingStatement.resultScoreRaw AS REAL),0) / COALESCE(cbMaxPoints,0) * 
                            100)  END) AS cacheFinalWeightScoreWithPenalty, 
                     
                   
               0 AS lastCsnChecked
         FROM ClazzAssignment
              JOIN ClazzEnrolment
              ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
              
               JOIN CourseBlock
                ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
               AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE} 
              
              LEFT JOIN StatementEntity AS SubmissionStatement
	          ON SubmissionStatement.statementUid = (SELECT statementUid 
                                   FROM StatementEntity
                                  WHERE StatementEntity.statementContentEntryUid = 0
                                    AND xObjectUid = ClazzAssignment.caXObjectUid
                                    AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                    AND StatementEntity.timestamp 
                                        BETWEEN CourseBlock.cbHideUntilDate
                                        AND CourseBlock.cbGracePeriodDate
                               ORDER BY timestamp DESC LIMIT 1
                                  )
              LEFT JOIN XObjectEntity AS ObjectStatementRef
              ON ObjectStatementRef.objectStatementRefUid = SubmissionStatement.statementUid                    
                                  
              LEFT JOIN StatementEntity AS MarkingStatement
               ON MarkingStatement.timestamp = (SELECT timestamp 
                                                  FROM StatementEntity 
                                                 WHERE xObjectUid = ObjectStatementRef.xObjectUid 
                                              ORDER BY timestamp DESC 
                                                 LIMIT 1)
              
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
        DELETE
         FROM ClazzAssignmentRollUp
        WHERE cacheContentEntryUid 
                IN (SELECT cacjContentUid 
                     FROM ClazzAssignmentContentJoin
                    WHERE NOT cacjActive)
           OR (cacheClazzAssignmentUid 
              IN (SELECT caUid 
                   FROM ClazzAssignment
                  WHERE caUid = :caUid
                    AND NOT caRequireFileSubmission) 
               AND cacheContentEntryUid = 0)                                        
    """)
    abstract suspend fun deleteCachedInactiveContent(caUid: Long)


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