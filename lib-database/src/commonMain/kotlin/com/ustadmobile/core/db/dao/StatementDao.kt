package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_LAST_ACTIVE_ASC
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_LAST_ACTIVE_DESC
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.StatementDaoCommon.SORT_LAST_NAME_DESC
import com.ustadmobile.door.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class StatementDao : BaseDao<StatementEntity> {

    @Query("""
     REPLACE INTO StatementEntityReplicate(sePk, seDestination)
      SELECT DISTINCT StatementEntity.statementUid AS sePk,
             :newNodeId AS seDestination
        FROM UserSession
             JOIN PersonGroupMember
                  ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
             JOIN ScopedGrant
                  ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
                     AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
             JOIN StatementEntity
                 ON ${StatementEntity.FROM_SCOPEDGRANT_TO_STATEMENT_JOIN_ON_CLAUSE}
       WHERE UserSession.usClientNodeId = :newNodeId
         AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
         -- Temporary measure to prevent admin user getting clogged up
         -- Restrict to the last 30 days of data
         AND StatementEntity.timestamp > ( 
       --notpsql
       strftime('%s', 'now') * 1000
       --endnotpsql
       /*psql
       ROUND(EXTRACT(epoch from NOW())*1000)
       */
       - (30 * CAST(86400000 AS BIGINT)))
       --notpsql
         AND StatementEntity.statementLct != COALESCE(
             (SELECT seVersionId
                FROM StatementEntityReplicate
               WHERE sePk = StatementEntity.statementUid
                 AND seDestination = UserSession.usClientNodeId), 0)
       --endnotpsql           
      /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
             SET sePending = (SELECT StatementEntity.statementLct
            FROM StatementEntity
           WHERE StatementEntity.statementUid = EXCLUDED.sePk ) 
                 != StatementEntityReplicate.seVersionId
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([StatementEntity::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO StatementEntityReplicate(sePk, seDestination)
  SELECT DISTINCT StatementEntity.statementUid AS seUid,
         UserSession.usClientNodeId AS seDestination
    FROM ChangeLog
         JOIN StatementEntity
               ON ChangeLog.chTableId = ${StatementEntity.TABLE_ID}
                  AND ChangeLog.chEntityPk = StatementEntity.statementUid
         JOIN ScopedGrant
              ON ${StatementEntity.FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
                 AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
         JOIN PersonGroupMember
              ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
         JOIN UserSession
              ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                 AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId
           FROM SyncNode
          LIMIT 1)
     AND StatementEntity.statementLct != COALESCE(
         (SELECT seVersionId
            FROM StatementEntityReplicate
           WHERE sePk = StatementEntity.statementUid
             AND seDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(sePk, seDestination) DO UPDATE
     SET sePending = true
  */
    """)
    @ReplicationRunOnChange([StatementEntity::class])
    @ReplicationCheckPendingNotificationsFor([StatementEntity::class])
    abstract suspend fun replicateOnChange()

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    abstract fun getOneStatement(): LiveData<StatementEntity?>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @RawQuery
    abstract suspend fun getResults(query: DoorQuery): List<StatementReportData>

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    @QueryLiveTables(["StatementEntity", "Person", "XLangMapEntry"])
    abstract fun getListResults(query: DoorQuery): DataSourceFactory<Int, StatementEntityWithDisplayDetails>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("SELECT * FROM XLangMapEntry LIMIT 1")
    abstract fun getXLangMap(): XLangMapEntry?


    @Query("""
        UPDATE StatementEntity 
           SET extensionProgress = :progress,
               statementLct = :updateTime 
            WHERE statementUid = :uid""")
    abstract fun updateProgress(uid: Long, progress: Int, updateTime: Long)


    @Query("""
        SELECT ResultSource.personUid, ResultSource.firstNames, ResultSource.lastName,
            COUNT(DISTINCT(ResultSource.contextRegistration)) AS attempts, 
            MIN(ResultSource.timestamp) AS startDate, 
            MAX(ResultSource.timestamp) AS endDate, 
            SUM(ResultSource.resultDuration) AS duration, 
            MAX(CASE WHEN ResultSource.contentEntryRoot 
                THEN resultScoreRaw
                ELSE 0 END) AS resultScore, 
            MAX(CASE WHEN ResultSource.contentEntryRoot 
                THEN resultScoreMax
                ELSE 0 END) AS resultMax,   
            MAX(CASE WHEN ResultSource.contentEntryRoot 
                THEN resultScoreScaled
                ELSE 0 END) AS resultScaled, 
            MAX(ResultSource.extensionProgress) AS progress,
            0 AS penalty,
            0 as resultWeight,
            'FALSE' AS contentComplete,
            0 AS success,
            
            CASE WHEN ResultSource.resultCompletion 
                THEN 1 ELSE 0 END AS totalCompletedContent,
                
            1 as totalContent, 
            
            0 as fileSubmissionStatus, 
         
            '' AS latestPrivateComment
        
         FROM (SELECT Person.personUid, Person.firstNames, Person.lastName, 
            StatementEntity.contextRegistration, StatementEntity.timestamp, 
            StatementEntity.resultDuration, StatementEntity.resultScoreRaw, 
            StatementEntity.resultScoreMax, StatementEntity.resultScoreScaled,
            StatementEntity.contentEntryRoot, StatementEntity.extensionProgress, 
            StatementEntity.resultCompletion
            FROM PersonGroupMember
            ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1} ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT} ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
             LEFT JOIN StatementEntity 
                ON StatementEntity.statementPersonUid = Person.personUid 
                    WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid 
                        AND PersonGroupMember.groupMemberActive  
                        AND statementContentEntryUid = :contentEntryUid
                        AND Person.firstNames || ' ' || Person.lastName LIKE :searchText              
                   GROUP BY StatementEntity.statementUid 
                   ORDER BY resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC) AS ResultSource 
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
    @SqliteOnly //This would need a considered group by to work on postgres
    abstract fun findPersonsWithContentEntryAttempts(contentEntryUid: Long, accountPersonUid: Long,
                                                     searchText: String, sortOrder: Int)
            : DataSourceFactory<Int, PersonWithAttemptsSummary>


    @Query("""
        SELECT 
                COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                0 as resultWeight,
                
                COALESCE((CASE WHEN resultCompletion 
                THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                
                1 as totalContent, 
                0 as penalty
                
        FROM ContentEntry
            LEFT JOIN StatementEntity
							ON StatementEntity.statementUid = 
                                (SELECT statementUid 
							       FROM StatementEntity 
                                  WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
							        AND StatementEntity.statementPersonUid = :accountPersonUid
							        AND contentEntryRoot 
                               ORDER BY resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC LIMIT 1)
                               
       WHERE contentEntryUid = :contentEntryUid
    """)
    abstract suspend fun getBestScoreForContentForPerson(contentEntryUid: Long, accountPersonUid: Long): ContentEntryStatementScoreProgress?


    @Query("""
         SELECT COALESCE((
                SELECT DISTINCT(statementpersonUid)
                  FROM ClazzAssignment 
                      JOIN ClazzEnrolment
                       ON ClazzEnrolment.clazzEnrolmentClazzUid = ClazzAssignment.caClazzUid
                       
                       JOIN CourseBlock
                       ON CourseBlock.cbEntityUid = ClazzAssignment.caUid
                       AND CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                       
          	           JOIN StatementEntity AS SubmissionStatement
          	           ON SubmissionStatement.statementUid = (SELECT statementUid 
                                   FROM StatementEntity
                                  WHERE StatementEntity.statementContentEntryUid = 0
                                    AND xObjectUid = ClazzAssignment.caXObjectUid
                                    AND StatementEntity.statementPersonUid = ClazzEnrolment.clazzEnrolmentPersonUid
                                    AND StatementEntity.timestamp 
                                        BETWEEN CourseBlock.cbHideUntilDate
                                        AND CourseBlock.cbGracePeriodDate
                               ORDER BY timestamp DESC LIMIT 1)
                               
          	           LEFT JOIN XObjectEntity
                       ON XObjectEntity.objectStatementRefUid = SubmissionStatement.statementUid  
               
                 WHERE ClazzAssignment.caUid = :assignmentUid
                   AND XObjectEntity.xobjectUid IS NULL
                   AND ClazzEnrolment.clazzEnrolmentActive
                   AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT}
                   AND ClazzEnrolment.clazzEnrolmentPersonUid != :currentStudentUid
            LIMIT 1),0)
    """)
    abstract suspend fun findNextStudentNotMarkedForAssignment(assignmentUid: Long,
                                                               currentStudentUid: Long): Long


    @Query("""
        SELECT * 
          FROM StatementEntity
         WHERE statementPersonUid = :studentUid
           AND statementVerbUid = ${VerbEntity.VERB_SUBMITTED_UID}
           AND xObjectUid = :assignmentObjectUid    
      ORDER BY timestamp                
    """)
    abstract suspend fun findSubmittedStatementFromStudent(studentUid: Long, assignmentObjectUid: Long): StatementEntity?

    @Query("""
        SELECT * 
          FROM StatementEntity
         WHERE statementPersonUid = :studentUid
           AND statementVerbUid = ${VerbEntity.VERB_SCORED_UID}
      ORDER BY timestamp                
    """)
    abstract fun findScoreStatementForStudent(studentUid: Long): StatementEntity?


    @Query("""
        SELECT MIN(timestamp) AS startDate, 
            MAX(CASE 
                    WHEN StatementEntity.resultSuccess > 0 
                    AND StatementEntity.contentEntryRoot 
                    THEN StatementEntity.resultSuccess 
                    ELSE 0 END) AS resultSuccess, 
            SUM(CASE 
                     WHEN CAST(resultCompletion AS INTEGER) > 0 
                     AND StatementEntity.contentEntryRoot 
                     THEN 1 
                     ELSE 0 END) AS resultComplete, 
            SUM(resultDuration) AS duration, contextRegistration, 
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreRaw ELSE 0 END) AS resultScore, 
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreMax ELSE 0 END) AS resultMax,
            MAX(CASE WHEN contentEntryRoot 
                     THEN resultScoreScaled ELSE 0 END) AS resultScoreScaled,
                       
            SUM(CASE WHEN resultCompletion AND StatementEntity.contentEntryRoot 
                THEN 1 ELSE 0 END) AS totalCompletedContent,
                
             1 as totalContent          
                       
        FROM StatementEntity 
             JOIN ScopedGrant 
                 ON ${StatementEntity.FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
                 AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
             JOIN PersonGroupMember 
                 ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid  
                AND PersonGroupMember.groupMemberPersonUid = :accountPersonUid
        WHERE statementContentEntryUid = :contentEntryUid   
          AND statementPersonUid = :personUid 
        GROUP BY StatementEntity.contextRegistration 
        ORDER BY startDate DESC, resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC
         """)
    @SqliteOnly
    abstract fun findSessionsForPerson(contentEntryUid: Long, accountPersonUid: Long, personUid: Long)
            : DataSourceFactory<Int, PersonWithSessionsDisplay>


    @Query("""
        SELECT StatementEntity.*, VerbEntity.*, 
            verbLangMap.valueLangMap AS verbDisplay, 
            xobjectMap.valueLangMap AS objectDisplay 
        FROM StatementEntity
                 JOIN ScopedGrant 
                    ON ${StatementEntity.FROM_STATEMENT_TO_SCOPEDGRANT_JOIN_ON_CLAUSE}
                    AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT}) > 0
                 JOIN PersonGroupMember 
                    ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid  
                AND PersonGroupMember.groupMemberPersonUid = :accountPersonUid
                LEFT JOIN VerbEntity 
                    ON VerbEntity.verbUid = StatementEntity.statementVerbUid 
                LEFT JOIN XLangMapEntry verbLangMap 
                    ON verbLangMap.verbLangMapUid = VerbEntity.verbUid
                LEFT JOIN XLangMapEntry xobjectMap 
                    ON xobjectMap.objectLangMapUid = StatementEntity.xObjectUid
         WHERE statementContentEntryUid = :contentEntryUid 
            AND statementPersonUid = :personUid 
            AND contextRegistration = :contextRegistration 
         ORDER BY StatementEntity.timestamp DESC
         """)
    abstract fun findSessionDetailForPerson(contentEntryUid: Long, accountPersonUid: Long,
                                            personUid: Long, contextRegistration: String)
            : DataSourceFactory<Int, StatementWithSessionDetailDisplay>


    @Query("""
        SELECT SUM(resultScoreRaw) AS resultScore, 
               SUM(resultScoreMax) AS resultMax,
               MAX(extensionProgress) AS progress,
               0 as resultWeight,
               0 as penalty,
               0 as success,
               'FALSE' as contentComplete,
               0 AS resultScaled, 
               COALESCE((CASE WHEN resultCompletion 
               THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                
                1 as totalContent
               
         FROM (SELECT * 
                 FROM StatementEntity 
                WHERE contextRegistration = :contextRegistration
                  AND NOT contentEntryRoot
                  AND statementVerbUid = ${VerbEntity.VERB_ANSWERED_UID} 
             GROUP BY xObjectUid) AS SessionStatements
    """)
    @SqliteOnly
    abstract suspend fun calculateScoreForSession(contextRegistration: String): ContentEntryStatementScoreProgress?


    @Query("""
        SELECT resultScoreRaw AS resultScore, 
               resultScoreMax AS resultMax,
               extensionProgress AS progress,
               0 AS penalty,
               0 as resultWeight,
               resultSuccess AS success,
               resultCompletion AS contentComplete, 
               resultScoreScaled AS resultScaled,
                1 AS totalCompletedContent,
                1 as totalContent
               
          FROM StatementEntity
         WHERE resultCompletion
          AND contextRegistration = :contextRegistration
          AND contentEntryRoot
     ORDER BY resultScoreScaled DESC, 
              extensionProgress DESC, 
              resultSuccess DESC 
              LIMIT 1
    """)
    abstract suspend fun findCompletedScoreForSession(contextRegistration: String): ContentEntryStatementScoreProgress?


    @Query("""
        SELECT contextRegistration 
          FROM StatementEntity
         WHERE statementPersonUid = :accountPersonUid
           AND statementContentEntryUid = :entryUid
           AND NOT EXISTS (SELECT statementUid FROM StatementEntity
                            WHERE statementPersonUid = :accountPersonUid
                             AND statementContentEntryUid = :entryUid
                             AND (statementVerbUid = ${VerbEntity.VERB_COMPLETED_UID} 
                                    OR statementVerbUid = ${VerbEntity.VERB_SATISFIED_UID}))
      ORDER BY timestamp DESC 
    """)
    abstract suspend fun findLatestRegistrationStatement(accountPersonUid: Long, entryUid: Long): String?




}
