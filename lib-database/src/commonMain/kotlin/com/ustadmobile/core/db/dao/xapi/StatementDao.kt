package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndRelated
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.StatementEntityAndDisplayDetails
import com.ustadmobile.lib.db.entities.StatementReportData
import com.ustadmobile.lib.db.entities.XLangMapEntry
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class StatementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entityList: List<StatementEntity>)

    @Query("SELECT * From StatementEntity LIMIT 1")
    abstract fun getOneStatement(): Flow<StatementEntity?>

    @RawQuery
    abstract suspend fun getResults(query: DoorQuery): List<StatementReportData>

    @RawQuery(observedEntities = [StatementEntity::class, Person::class, XLangMapEntry::class])
    @QueryLiveTables(["StatementEntity", "Person", "XLangMapEntry"])
    abstract fun getListResults(query: DoorQuery): PagingSource<Int, StatementEntityAndDisplayDetails>


    // This is required because of above raw query
    @Query("SELECT * FROM PERSON LIMIT 1")
    abstract fun getPerson(): Person?

    @Query("""
        SELECT StatementEntity.*
          FROM StatementEntity
         WHERE (    (:statementIdHi = 0 AND :statementIdLo = 0) 
                 OR (statementIdHi = :statementIdHi AND statementIdLo = :statementIdLo))
                  
    """)
    abstract suspend fun getStatements(
        statementIdHi: Long,
        statementIdLo: Long,
    ): List<StatementEntity>

    @Query("""
        SELECT StatementEntity.*
          FROM StatementEntity
         WHERE statementIdHi = :statementIdHi 
           AND statementIdLo = :statementIdLo       
    """)
    abstract suspend fun findById(
        statementIdHi: Long,
        statementIdLo: Long,
    ): StatementEntity?


    /**
     * Find Xapi Statements that are relevant to determining the completion status of a
     * given ContentEntry for a given user (e.g. they match the content entry, person,
     * StatementEntity.contentEntryRoot is true, and (progress > 0 OR completion = true)
     */
    @Query("""
        SELECT StatementEntity.*
               $FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY
    """)
    abstract suspend fun findStatusStatementsByContentEntryUid(
        contentEntryUid: Long,
        courseBlockUid: Long,
        accountPersonUid: Long,
    ): List<StatementEntity>

    @Query("""
        SELECT StatementEntity.*
               $FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT
    """)
    abstract suspend fun findStatusStatementByParentContentEntryUid(
        parentUid: Long,
        accountPersonUid: Long,
    ): List<StatementEntity>


    @Query("""
        SELECT StatementEntity.*
               $FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
    """)
    abstract suspend fun findStatusStatementsForStudentByClazzUid(
        clazzUid: Long,
        accountPersonUid: Long,
    ): List<StatementEntity>

    @Query("""
        WITH PersonUidsAndCourseBlocks(personUid, username, cbUid, cbType, caMarkingType) AS (
             SELECT Person.personUid AS personUid, 
                    Person.username AS username, 
                    CourseBlock.cbUid AS cbUid,
                    CourseBlock.cbType AS cbType,
                    ClazzAssignment.caMarkingType AS caMarkingType
               FROM Person
                    JOIN CourseBlock
                         ON CourseBlock.cbClazzUid = :clazzUid
                    LEFT JOIN ClazzAssignment
                         ON CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                        AND ClazzAssignment.caUid = CourseBlock.cbEntityUid     
              WHERE Person.personUid IN 
                    (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid
                       FROM ClazzEnrolment
                      WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid)       
        )
        
        -- Maximum score statement
        SELECT StatementEntity_Outer.*, ActorEntity_Outer.*
          FROM PersonUidsAndCourseBlocks
               JOIN StatementEntity StatementEntity_Outer
                    ON (StatementEntity_Outer.statementIdHi, StatementEntity_Outer.statementIdLo) IN (
                        SELECT StatementEntity.statementIdHi, StatementEntity.statementIdLo
                          FROM StatementEntity
                               JOIN ActorEntity
                                    ON StatementEntity.statementActorUid = ActorEntity.actorUid
                         WHERE $STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS 
                      ORDER BY StatementEntity.extensionProgress DESC
                         LIMIT 1
                    )
               JOIN ActorEntity ActorEntity_Outer
                    ON ActorEntity_Outer.actorUid = StatementEntity_Outer.statementActorUid
                    
        UNION
        
        --Completed statement
        SELECT StatementEntity_Outer.*, ActorEntity_Outer.*
          FROM PersonUidsAndCourseBlocks
               JOIN StatementEntity StatementEntity_Outer
                    ON (StatementEntity_Outer.statementIdHi, StatementEntity_Outer.statementIdLo) IN (
                          SELECT StatementEntity.statementIdHi, StatementEntity.statementIdLo
                            FROM StatementEntity
                                 JOIN ActorEntity
                                      ON StatementEntity.statementActorUid = ActorEntity.actorUid
                           WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                             AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
                           LIMIT 1     
                    )
               JOIN ActorEntity ActorEntity_Outer
                    ON ActorEntity_Outer.actorUid = StatementEntity_Outer.statementActorUid    
        UNION 
        
        -- StatementEntity for success or fail e.g. resultSuccess is not null             
        SELECT StatementEntity_Outer.*, ActorEntity_Outer.*
          FROM PersonUidsAndCourseBlocks
               JOIN StatementEntity StatementEntity_Outer
                    ON (StatementEntity_Outer.statementIdHi, StatementEntity_Outer.statementIdLo) IN (
                          SELECT StatementEntity.statementIdHi, StatementEntity.statementIdLo
                            FROM StatementEntity
                                 JOIN ActorEntity
                                      ON StatementEntity.statementActorUid = ActorEntity.actorUid
                           WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                             AND StatementEntity.resultSuccess IS NOT NULL
                        ORDER BY StatementEntity.resultSuccess DESC     
                           LIMIT 1     
                    )
               JOIN ActorEntity ActorEntity_Outer
                    ON ActorEntity_Outer.actorUid = StatementEntity_Outer.statementActorUid    
        
        UNION
        
        --StatementEntity for score
        SELECT StatementEntity_Outer.*, ActorEntity_Outer.*
          FROM PersonUidsAndCourseBlocks
               JOIN StatementEntity StatementEntity_Outer
                    ON (StatementEntity_Outer.statementIdHi, StatementEntity_Outer.statementIdLo) IN (
                        SELECT StatementEntity.statementIdHi, StatementEntity.statementIdLo
                          FROM StatementEntity
                               JOIN ActorEntity
                                    ON StatementEntity.statementActorUid = ActorEntity.actorUid
                               -- Where there is a peer marked assignment  - Work in progress    
                         WHERE (    PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                                AND PersonUidsAndCourseBlocks.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                                AND (1 + 1 = 5))
                                -- Where this is an assignment marked by teacher
                            OR (    PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                                AND PersonUidsAndCourseBlocks.caMarkingType = ${ClazzAssignment.MARKED_BY_COURSE_LEADER}
                                AND StatementEntity.resultScoreScaled IS NOT NULL
                                AND StatementEntity.timestamp = (
                                    SELECT MAX(StatementEntityInner.timestamp)
                                      FROM StatementEntity StatementEntityInner
                                           JOIN ActorEntity ActorEntityInner
                                                ON StatementEntityInner.statementActorUid = ActorEntityInner.actorUid
                                     WHERE StatementEntityInner.statementCbUid = PersonUidsAndCourseBlocks.cbUid
                                       AND ActorEntityInner.actorAccountName = PersonUidsAndCourseBlocks.username
                                       AND StatementEntityInner.resultScoreScaled IS NOT NULL
                                    )
                                )
                                -- This is self-paced content so take the best score
                                -- note should check root attribute
                            OR  (    PersonUidsAndCourseBlocks.cbType != ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                                 AND StatementEntity.resultScoreScaled IS NOT NULL
                                 AND StatementEntity.resultScoreScaled = (
                                     SELECT MAX(StatementEntityInner.resultScoreScaled)
                                       FROM StatementEntity StatementEntityInner
                                            JOIN ActorEntity ActorEntityInner
                                                ON StatementEntityInner.statementActorUid = ActorEntityInner.actorUid
                                      WHERE StatementEntityInner.statementCbUid = PersonUidsAndCourseBlocks.cbUid
                                       AND ActorEntityInner.actorAccountName = PersonUidsAndCourseBlocks.username
                                       AND StatementEntityInner.resultScoreScaled IS NOT NULL) 
                                )        
                    )
               JOIN ActorEntity ActorEntity_Outer
                    ON ActorEntity_Outer.actorUid = StatementEntity_Outer.statementActorUid         
    """)
    abstract suspend fun findStatusForStudentsInClazzStatements(
        clazzUid: Long,
    ): List<StatementEntityAndRelated>


    //To pull over http - change these to selecting the statementuid(s)
    @Query("""
        SELECT Person.personUid AS sPersonUid,
               CourseBlock.cbUid AS sCbUid,
               (SELECT MAX(StatementEntity.extensionProgress)
                  FROM StatementEntity
                       JOIN ActorEntity
                            ON StatementEntity.statementActorUid = ActorEntity.actorUid
                 WHERE $STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE 
               ) AS sProgress,
               (SELECT EXISTS(
                       SELECT 1
                         FROM StatementEntity
                              JOIN ActorEntity
                                   ON StatementEntity.statementActorUid = ActorEntity.actorUid
                        WHERE ($STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE)
                          AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
               )) AS sIsCompleted,
               (SELECT CASE
                       /*If there is a statement marked as success, then count as successful even if
                        *there were subsequent failed attempts
                        */
                       WHEN (
                            SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                           JOIN ActorEntity
                                                ON StatementEntity.statementActorUid = ActorEntity.actorUid
                                    WHERE ($STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE)
                                      AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1
                                   )                           
                       ) THEN 1
                       /*If there are no statements marked as success, however there are statements marekd as fail,
                        *then count as fail 
                        */
                       WHEN (
                            SELECT EXISTS(
                                    SELECT 1
                                      FROM StatementEntity
                                           JOIN ActorEntity
                                                ON StatementEntity.statementActorUid = ActorEntity.actorUid
                                    WHERE ($STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE)
                                      AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0
                                   )                           
                       ) THEN 0
                       /* Else there is no known success/fail result*/
                       ELSE NULL
                       END
               ) AS sIsSuccess,
               -- See ClazzGradebookScreen for info on which score is selected
               (SELECT CASE
                       -- When there is a peer marked assignment, take the average of the latest distinct ...
                       WHEN (     CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                              AND ClazzAssignment.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS} 
                            ) 
                            THEN 0
                       -- When an assignment, but not peer marked, then the latest score     
                       WHEN CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                            THEN (SELECT StatementEntity.resultScoreScaled
                                    FROM StatementEntity
                                         JOIN ActorEntity
                                              ON StatementEntity.statementActorUid = ActorEntity.actorUid
                                   WHERE ($STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE)
                                ORDER BY StatementEntity.timestamp DESC
                                   LIMIT 1)
                       -- else the best score accomplished so far            
                       ELSE (SELECT MAX(StatementEntity.resultScoreScaled) 
                               FROM StatementEntity
                                    JOIN ActorEntity
                                         ON StatementEntity.statementActorUid = ActorEntity.actorUid
                              WHERE ($STATEMENT_MATCHES_PERSON_AND_COURSEBLOCK_CLAUSE))            
                       END
               ) AS sScoreScaled
          FROM Person
               JOIN CourseBlock
                    ON CourseBlock.cbClazzUid = :clazzUid
               LEFT JOIN ClazzAssignment
                    ON CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                       AND ClazzAssignment.caUid = CourseBlock.cbEntityUid
         WHERE Person.personUid IN (:studentPersonUids)
    """)
    abstract suspend fun findStatusForStudentsInClazz(
        clazzUid: Long,
        studentPersonUids: List<Long>,
    ): List<BlockStatus>


}