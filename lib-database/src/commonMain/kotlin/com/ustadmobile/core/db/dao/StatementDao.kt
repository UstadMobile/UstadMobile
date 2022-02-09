package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import com.ustadmobile.door.*
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@Dao
@Repository
abstract class StatementDao : BaseDao<StatementEntity> {

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
    abstract fun getOneStatement(): DoorLiveData<StatementEntity?>

    @Query("SELECT * FROM StatementEntity WHERE statementId = :id LIMIT 1")
    abstract fun findByStatementId(id: String): StatementEntity?

    @Query("SELECT * FROM StatementEntity WHERE statementId IN (:id)")
    abstract fun findByStatementIdList(id: List<String>): List<StatementEntity>

    @RawQuery
    abstract suspend fun getResults(query: DoorQuery): List<ReportData>

    open suspend fun getResults(sqlStr: String, paramsList: Array<Any>): List<ReportData> {
        return getResults(SimpleDoorQuery(sqlStr, paramsList))
    }

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    @QueryLiveTables(["StatementEntity", "Person", "XLangMapEntry"])
    abstract fun getListResults(query: DoorQuery): DoorDataSourceFactory<Int, StatementEntityWithDisplayDetails>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("SELECT * FROM XLangMapEntry LIMIT 1")
    abstract fun getXLangMap(): XLangMapEntry?


    @Query("""UPDATE StatementEntity SET extensionProgress = :progress,
            statementLastChangedBy = ${SyncNode.SELECT_LOCAL_NODE_ID_SQL} 
            WHERE statementUid = :uid""")
    abstract fun updateProgress(uid: Long, progress: Int)


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
            'FALSE' AS contentComplete,
            0 AS success,
            
            CASE WHEN ResultSource.resultCompletion 
                THEN 1 ELSE 0 END AS totalCompletedContent,
                
            1 as totalContent, 
            
             
         
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
            : DoorDataSourceFactory<Int, PersonWithAttemptsSummary>


    @Query("""
        SELECT 
                COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                
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
            : DoorDataSourceFactory<Int, PersonWithSessionsDisplay>


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
            : DoorDataSourceFactory<Int, StatementWithSessionDetailDisplay>


    @Query("""
        SELECT SUM(resultScoreRaw) AS resultScore, 
               SUM(resultScoreMax) AS resultMax,
               MAX(extensionProgress) AS progress,
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


    @Serializable
    data class ReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")

    companion object{

        const val SORT_FIRST_NAME_ASC = 1

        const val SORT_FIRST_NAME_DESC = 2

        const val SORT_LAST_NAME_ASC = 3

        const val SORT_LAST_NAME_DESC = 4

        const val SORT_LAST_ACTIVE_ASC = 5

        const val SORT_LAST_ACTIVE_DESC = 6


    }


}
