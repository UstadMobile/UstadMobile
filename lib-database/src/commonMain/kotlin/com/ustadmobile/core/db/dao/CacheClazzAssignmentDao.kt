package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.CacheClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzEnrolment

@Dao
@Repository
abstract class CacheClazzAssignmentDao: BaseDao<CacheClazzAssignment> {

    @Query("""
        REPLACE INTO CacheClazzAssignment 
                (cachePersonUid, cacheContentEntryUid, cacheClazzAssignmentUid, 
                 cacheStudentScore, cacheMaxScore, cacheProgress, 
                 cacheContentComplete, cacheSuccess,cachePenalty, lastCsnChecked)
       
       SELECT clazzEnrolmentPersonUid AS cachePersonUid, 
                cacjContentUid AS cacheContentEntryUid, caUid AS cacheClazzAssignmentUid, 
               COALESCE(resultScoreRaw,0) AS cacheStudentScore, 
               COALESCE(resultScoreMax,0) AS cacheMaxScore,
               COALESCE(extensionProgress,0) AS cacheProgress,
               COALESCE(resultCompletion,'FALSE') AS cacheContentComplete, 
               COALESCE(resultSuccess,0) AS cacheSuccess,
               (CASE WHEN timestamp > caDeadlineDate 
                     THEN caLateSubmissionPenalty 
                     ELSE 0 END) AS cachePenalty,
               (SELECT MAX(statementLocalChangeSeqNum) FROM StatementEntity) AS lastCsnChecked
          FROM ClazzAssignmentContentJoin
	            LEFT JOIN ClazzAssignment 
                ON ClazzAssignment.caUid = ClazzAssignmentContentJoin.cacjAssignmentUid
                                
                LEFT JOIN ClazzEnrolment
                ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
					      	      
			    LEFT JOIN StatementEntity 
	            ON statementUid = (SELECT statementUid 
                                     FROM StatementEntity 
                                            LEFT JOIN ClazzAssignment 
                                            ON caUid = ClazzAssignmentContentJoin.cacjAssignmentUid 
                                    WHERE statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid
                                      AND statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                      AND contentEntryRoot 
                                      AND StatementEntity.timestamp 
                                            BETWEEN ClazzAssignment.caStartDate
                                            AND ClazzAssignment.caGracePeriodDate
                                  ORDER BY CASE WHEN timestamp > ClazzAssignment.caDeadlineDate 
                                                THEN resultScoreScaled * (1 - (caLateSubmissionPenalty/100))
                                                ELSE resultScoreScaled END DESC, 
                                            extensionProgress DESC LIMIT 1)
	     WHERE clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT} 
           AND clazzEnrolmentOutcome = ${ClazzEnrolment.OUTCOME_IN_PROGRESS}
           AND clazzEnrolmentActive
           AND caActive
           AND cacjActive
           AND (:clazzUid = 0 OR ClazzAssignment.caClazzUid = :clazzUid)
           AND (:assignmentUid = 0 OR ClazzAssignment.caUid = :assignmentUid)
           AND (:personUid = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :personUid)
           AND lastCsnChecked >= COALESCE((SELECT MAX(lastCsnChecked) FROM CacheClazzAssignment),0)
      GROUP BY cacheClazzAssignmentUid, cacheContentEntryUid, cachePersonUid
    """)
    abstract suspend fun cacheBestStatements(clazzUid: Long, assignmentUid: Long, personUid: Long)

    @Query("""
        DELETE
         FROM CacheClazzAssignment
        WHERE cacheContentEntryUid 
                IN (SELECT cacjContentUid 
                     FROM ClazzAssignmentContentJoin
                    WHERE NOT cacjActive)
    """)
    abstract suspend fun deleteCachedInactiveContent()


    @Query("""
        UPDATE CacheClazzAssignment 
           SET lastCsnChecked = 0
         WHERE cacheClazzAssignmentUid = :changedAssignmentUid
    """)
    abstract suspend fun invalidateCacheByAssignment(changedAssignmentUid: Long)

    @Query("""
        UPDATE CacheClazzAssignment 
           SET lastCsnChecked = 0
         WHERE cacheClazzAssignmentUid IN (:changedAssignmentUid)
    """)
    abstract suspend fun invalidateCacheByAssignmentList(changedAssignmentUid: List<Long>)

}