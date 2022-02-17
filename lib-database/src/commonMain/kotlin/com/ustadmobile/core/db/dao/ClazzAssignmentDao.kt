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
abstract class ClazzAssignmentDao : BaseDao<ClazzAssignment> {

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
                                  )
                         AND (NOT ClazzAssignment.caRequireFileSubmission 
                              OR NOT EXISTS
                              (SELECT statementUid
                                 FROM StatementEntity
                                WHERE StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                  AND StatementEntity.xObjectUid = ClazzAssignment.caXObjectUid
                                  AND StatementEntity.timestamp
                                      BETWEEN ClazzAssignment.caStartDate
                                      AND ClazzAssignment.caGracePeriodDate)))             
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
                                      WHERE ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid)
                           AND (NOT ClazzAssignment.caRequireFileSubmission 
                              OR EXISTS
                              (SELECT statementUid
                                 FROM StatementEntity
                                WHERE StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                  AND StatementEntity.xObjectUid = ClazzAssignment.caXObjectUid
                                  AND StatementEntity.timestamp
                                      BETWEEN ClazzAssignment.caStartDate
                                      AND ClazzAssignment.caGracePeriodDate)))            
                  ELSE 0 END) AS completedStudents,
                   
                    (CASE WHEN (SELECT hasPermission 
                         FROM CtePermissionCheck)
                  THEN (SELECT COUNT(DISTINCT(StatementEntity.statementPersonUid)) 
                           FROM StatementEntity 
                                JOIN XObjectEntity 
                                ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid 
                                
                                JOIN StatementEntity as SubmissionStatement 
                                ON SubmissionStatement.statementId = XObjectEntity.objectId
                                
                                JOIN ClazzEnrolment
                                ON ClazzEnrolment.clazzEnrolmentPersonUid = StatementEntity.statementPersonUid
                               
                          WHERE SubmissionStatement.xObjectUid = ClazzAssignment.caXObjectUid
                            AND ClazzEnrolment.clazzEnrolmentActive
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                            AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft)
                    ELSE 0 END) AS markedStudents,          
                   
                   
           
            0 AS resultScore,
                          
            0 AS resultMax,
                                
            COALESCE((SELECT COUNT(cacheContentComplete)
                        FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                        AND cacheContentComplete
                              AND cachePersonUid = :accountPersonUid)  =  
                          
                          (SELECT COUNT(DISTINCT cacheContentEntryUid) 
                             FROM ClazzAssignmentRollUp
                            WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid), 'FALSE') AS contentComplete,
                          
            0 AS penalty,
                                               
            $GET_TOTAL_COMPLETE_CONTENT_OF_ASSIGNMENT_FOR_USER AS totalCompletedContent,
            
            $GET_TOTAL_CONTENT_OF_ASSIGNMENT AS totalContent,                    
                          
             0 as success,           
             $GET_TOTAL_SCORE_WITH_PENALTY_FOR_USER_IN_ASSIGNMENT AS resultScaled,
                              
             $GET_TOTAL_WEIGHT_OF_ASSIGNMENT AS resultWeight,                    
              
              COALESCE((SELECT AVG(cacheProgress)
                        FROM ClazzAssignmentRollUp
                       WHERE cacheClazzAssignmentUid = ClazzAssignment.caUid
                         AND cachePersonUid = :accountPersonUid), 0) AS progress
             
             
             FROM ClazzAssignment
            WHERE ClazzAssignment.caActive
              AND ClazzAssignment.caClazzUid = :clazzUid
              AND (ClazzAssignment.caTitle LIKE :searchText 
                    OR ClazzAssignment.caDescription LIKE :searchText)
              AND ((SELECT hasPermission FROM CtePermissionCheck) OR :timestamp >= ClazzAssignment.caStartDate)
         ORDER BY CASE(:sortOrder)
                WHEN $SORT_START_DATE_ASC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_ASC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_ASC THEN (($GET_TOTAL_SCORE_WITH_PENALTY_FOR_USER_IN_ASSIGNMENT/$GET_TOTAL_WEIGHT_OF_ASSIGNMENT) * ($GET_TOTAL_COMPLETE_CONTENT_OF_ASSIGNMENT_FOR_USER/$GET_TOTAL_CONTENT_OF_ASSIGNMENT))
                ELSE 0
            END ASC,
            CASE(:sortOrder)
                WHEN $SORT_START_DATE_DESC THEN ClazzAssignment.caStartDate
                WHEN $SORT_DEADLINE_DESC THEN ClazzAssignment.caDeadlineDate
                WHEN $SORT_SCORE_DESC THEN (($GET_TOTAL_SCORE_WITH_PENALTY_FOR_USER_IN_ASSIGNMENT/$GET_TOTAL_WEIGHT_OF_ASSIGNMENT) * ($GET_TOTAL_COMPLETE_CONTENT_OF_ASSIGNMENT_FOR_USER/$GET_TOTAL_CONTENT_OF_ASSIGNMENT))
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
        SELECT 0 AS resultMax, 
               0 AS resultScore, 
               COALESCE(SUM(ResultSource.cacheFinalWeightScoreWithPenalty),0) as resultScaled,
               'FALSE' as contentComplete, 
               COALESCE(AVG(ResultSource.cacheProgress),0) as progress, 0 as success,
               0 AS penalty,
               
               COALESCE(SUM(ResultSource.cacheWeight),0) As resultWeight,
               
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
                
              COALESCE((SELECT SUM(cacheFinalWeightScoreWithPenalty)
                         FROM ClazzAssignmentRollUp 
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                          AND cachePersonUid = ResultSource.personUid), 0) AS resultScaled,
                              
             COALESCE((SELECT SUM(cacheWeight)
                         FROM ClazzAssignmentRollUp 
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                          AND cachePersonUid = ResultSource.personUid), 0) AS resultWeight,       
                
                'FALSE' as contentComplete,
                
                0 AS resultScore,
                          
                0 AS resultMax, 
                                        
                0 AS penalty,   
                                                    
                   COALESCE((SELECT COUNT(cacheContentComplete)
                        FROM ClazzAssignmentRollUp
                        WHERE cacheClazzAssignmentUid = :assignmentUid
                        AND cacheContentComplete
                              AND cachePersonUid = ResultSource.personUid), 0) AS totalCompletedContent,
            
            COALESCE((SELECT COUNT(DISTINCT cacheContentEntryUid) 
                              FROM ClazzAssignmentRollUp
                             WHERE cacheClazzAssignmentUid = :assignmentUid), 0) AS totalContent,                              
                                                   
           COALESCE((CASE WHEN NOT ResultSource.caRequireFileSubmission 
                          THEN ${ClazzAssignment.FILE_SUBMISSION_NOT_REQUIRED} 
                          WHEN MarkedStatement.statementUid IS NOT NULL 
                          THEN ${ClazzAssignment.FILE_MARKED} 
                          WHEN SubmissionStatement.statementUid IS NOT NULL 
                          THEN ${ClazzAssignment.FILE_SUBMITTED} 
                          ELSE ${ClazzAssignment.FILE_NOT_SUBMITTED} END), 
                               ${ClazzAssignment.FILE_SUBMISSION_NOT_REQUIRED} ) AS fileSubmissionStatus,
                                 

            cm.commentsText AS latestPrivateComment
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            StatementEntity.contextRegistration, StatementEntity.timestamp, 
            StatementEntity.resultDuration, ClazzAssignment.caXObjectUid, 
            ClazzAssignment.caRequireFileSubmission
                FROM PersonGroupMember
         ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             LEFT JOIN ClazzEnrolment
             ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid 
                AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
            
             LEFT JOIN ClazzAssignment 
             ON ClazzAssignment.caUid = :assignmentUid
             
             LEFT JOIN ClazzAssignmentContentJoin 
             ON ClazzAssignmentContentJoin.cacjAssignmentUid = ClazzAssignment.caUid
             AND ClazzAssignmentContentJoin.cacjActive
		                   
                          
             LEFT JOIN StatementEntity 
             ON StatementEntity.statementPersonUid = Person.personUid  
                AND StatementEntity.statementContentEntryUid = ClazzAssignmentContentJoin.cacjContentUid  
                AND StatementEntity.timestamp
                BETWEEN ClazzAssignment.caStartDate
                AND ClazzAssignment.caGracePeriodDate    
                
                
                WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                AND PersonGroupMember.groupMemberActive                      
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
                   LEFT JOIN StatementEntity AS SubmissionStatement
                   ON SubmissionStatement.statementUid = (SELECT statementUid 
                                   FROM StatementEntity
                                  WHERE StatementEntity.statementContentEntryUid = 0
                                    AND xObjectUid = ResultSource.caXObjectUid
                                    AND StatementEntity.statementPersonUid = ResultSource.personUid
                               ORDER BY timestamp DESC LIMIT 1)
                   
                  
                   LEFT JOIN XObjectEntity
                   ON SubmissionStatement.statementId = XObjectEntity.objectId
                   
                   LEFT JOIN StatementEntity AS MarkedStatement
                   ON MarkedStatement.statementPersonUid = ResultSource.personUid
                   AND MarkedStatement.timestamp = (SELECT timestamp 
                                                       FROM StatementEntity 
                                                      WHERE StatementEntity.xObjectUid = XObjectEntity.xObjectUid 
                                                   ORDER BY timestamp DESC 
                                                      LIMIT 1)
                   
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
                                  )
                         AND (NOT ClazzAssignment.caRequireFileSubmission 
                              OR NOT EXISTS
                              (SELECT statementUid
                                 FROM StatementEntity
                                WHERE StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                  AND StatementEntity.xObjectUid = ClazzAssignment.caXObjectUid
                                  AND StatementEntity.timestamp
                                      BETWEEN ClazzAssignment.caStartDate
                                      AND ClazzAssignment.caGracePeriodDate))) 
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
                                        AND cacjActive)
                           AND (NOT ClazzAssignment.caRequireFileSubmission 
                              OR EXISTS
                              (SELECT statementUid
                                 FROM StatementEntity
                                WHERE StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                  AND StatementEntity.xObjectUid = ClazzAssignment.caXObjectUid
                                  AND StatementEntity.timestamp
                                      BETWEEN ClazzAssignment.caStartDate
                                      AND ClazzAssignment.caGracePeriodDate)))               
                  ELSE 0 END) AS completedStudents,
                  
                  
             (CASE WHEN (SELECT hasPermission 
                           FROM CtePermissionCheck)
                   THEN (SELECT COUNT(DISTINCT(StatementEntity.statementPersonUid)) 
                           FROM StatementEntity 
                                JOIN XObjectEntity 
                                ON XObjectEntity.xObjectUid = StatementEntity.xObjectUid 
                                
                                JOIN StatementEntity as SubmissionStatement 
                                ON SubmissionStatement.statementId = XObjectEntity.objectId
                                
                                JOIN ClazzEnrolment
                                ON ClazzEnrolment.clazzEnrolmentPersonUid = StatementEntity.statementPersonUid
                               
                          WHERE SubmissionStatement.xObjectUid = ClazzAssignment.caXObjectUid
                            AND ClazzEnrolment.clazzEnrolmentActive
                            AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                            AND ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                            AND ClazzAssignment.caGracePeriodDate <= ClazzEnrolment.clazzEnrolmentDateLeft)
                   ELSE 0 END) AS markedStudents
                           
        
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