package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.PERSON_UIDS_FOR_PAGED_GRADEBOOK_QUERY_CTE
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.ACTOR_UIDS_FOR_PERSONUIDS_CTE
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.SELECT_STATUS_STATEMENTS_FOR_ACTOR_PERSON_UIDS
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndRelated
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.StatementEntityAndDisplayDetails
import com.ustadmobile.lib.db.entities.StatementReportData
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
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

    @RawQuery(observedEntities = [StatementEntity::class, Person::class])
    @QueryLiveTables(["StatementEntity", "Person"])
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
     * StatementEntity.completionOrProgress is true, and (progress > 0 OR completion = true)
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
        WITH PersonUids(personUid) AS (
            SELECT :accountPersonUid AS personUid
        ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE
        
        SELECT StatementEntity.*
               $FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
    """)
    abstract suspend fun findStatusStatementsForStudentByClazzUid(
        clazzUid: Long,
        accountPersonUid: Long,
    ): List<StatementEntity>

    @Query("""
        -- Get the PersonUids for those that are within the current page as per studentsLimit and 
        -- studentsOffset
        WITH $PERSON_UIDS_FOR_PAGED_GRADEBOOK_QUERY_CTE,
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE

        $SELECT_STATUS_STATEMENTS_FOR_ACTOR_PERSON_UIDS
    """)
    /**
     * This query will fetch the StatementEntity and related (e.g. ActorEntity, GroupMemberActorJoin)
     * required by ClazzGradebook to show the Gradebook results. The query uses the same parameters
     * as findByClazzUidAndRoleForGradebook (which is paged) so it can determine which PersonUids
     * it needs to fetch statements for via a CTE (PersonUids) to match the page (using the
     * studentsLimit and studentsoffset arguments).
     *
     * The query will match any statement that is matches students in the current page where the
     * statement provides a score, a completion status, or progress.
     */
    abstract suspend fun findStatusForStudentsInClazzStatements(
        clazzUid: Long,
        roleId: Int,
        sortOrder: Int,
        searchText: String? = "%",
        filter: Int,
        accountPersonUid: Long,
        currentTime: Long,
        studentsLimit: Int,
        studentsOffset: Int,
        completionOrProgressTrueVal: Boolean,
    ): List<StatementEntityAndRelated>


    /**
     * Get all the xapi statements required to determine the status of each block for a
     * given list of students in a given class.
     */
    @Query("""
        WITH PersonUids(personUid) AS (
            SELECT Person.personUid
              FROM Person
             WHERE Person.personUid IN (:studentPersonUids) 
        ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE
        
        $SELECT_STATUS_STATEMENTS_FOR_ACTOR_PERSON_UIDS
    """)
    abstract suspend fun findStatusForStudentsInClazzByUidList(
        clazzUid: Long,
        studentPersonUids: List<Long>,
        completionOrProgressTrueVal: Boolean,
    ): List<StatementEntityAndRelated>

    /**
     * Select the actor entities required for findStatusForStudentsInClazzByUidList .
     * When handling GroupAssignments the ActorEntity in StatementEntityAndRelated will be the
     * ActorEntity representing the group, so we need to get (separately) the ActorEntity that
     * represents the student.
     */
    @Query("""
        WITH PersonUids(personUid) AS (
            SELECT Person.personUid
              FROM Person
             WHERE Person.personUid IN (:studentPersonUids) 
        ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE
        
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorPersonUid IN 
               (SELECT PersonUids.personUid
                  FROM PersonUids)
           AND :clazzUid = :clazzUid
           AND :accountPersonUid = :accountPersonUid
    """)
    abstract suspend fun findActorEntitiesForStudentInClazzByUidList(
        clazzUid: Long,
        studentPersonUids: List<Long>,
        accountPersonUid: Long,
    ): List<ActorEntity>


    @Query(StatementDaoCommon.FIND_STATUS_FOR_STUDENTS_SQL)
    abstract suspend fun findStatusForStudentsInClazz(
        clazzUid: Long,
        studentPersonUids: List<Long>,
        accountPersonUid: Long,
    ): List<BlockStatus>

    /**
     * Used by ClazzDetailOverview to retrieve the BlockStatus for the current active user (if they
     * are student of the Clazz). We don't need to pull permission entities as this is already done
     * by ClazzDetailOverview checking for permission.
     *
     * NOTE: In next release accountpersonuid param will be used to enforce http permissions
     */
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findStatusForStudentsInClazzByUidList",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "completionOrProgressTrueVal",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall(
                functionName = "findActorEntitiesForStudentInClazzByUidList",
            )
        )
    )
    @Query(StatementDaoCommon.FIND_STATUS_FOR_STUDENTS_SQL)
    abstract fun findStatusForStudentsInClazzAsFlow(
        clazzUid: Long,
        studentPersonUids: List<Long>,
        accountPersonUid: Long,
    ): Flow<List<BlockStatus>>

    /**
     * Look for a registration that has not been completed.
     */
    @Query("""
        WITH MostRecentRegistration(statementIdHi, statementIdLo, contextRegistrationHi, contextRegistrationLo) AS (
             SELECT StatementEntity.statementIdHi, StatementEntity.contextRegistrationLo,
                    StatementEntity.contextRegistrationHi, StatementEntity.contextRegistrationLo
               FROM StatementEntity
              WHERE StatementEntity.statementActorUid = :actorUid
                AND StatementEntity.statementObjectUid1 = :activityUid
                AND StatementEntity.contextRegistrationHi != 0
           ORDER BY StatementEntity.timestamp DESC
              LIMIT 1
        )
        
        SELECT StatementEntity.*
          FROM StatementEntity
         WHERE StatementEntity.statementIdHi = (SELECT statementIdHi FROM MostRecentRegistration)
           AND StatementEntity.statementIdLo = (SELECT statementIdLo FROM MostRecentRegistration)
           AND NOT EXISTS(
                   SELECT 1
                     FROM StatementEntity StatementEntity_Inner
                    WHERE StatementEntity_Inner.statementActorUid = :actorUid
                      AND StatementEntity_Inner.statementObjectUid1 = :activityUid
                      AND StatementEntity_Inner.contextRegistrationHi = (SELECT contextRegistrationHi FROM MostRecentRegistration)
                      AND StatementEntity_Inner.contextRegistrationLo = (SELECT contextRegistrationLo FROM MostRecentRegistration)
                      AND CAST(StatementEntity_Inner.completionOrProgress AS INTEGER) = 1
                      AND (     StatementEntity_Inner.resultCompletion IS NOT NULL 
                            AND CAST(StatementEntity_Inner.resultCompletion AS INTEGER) = 1)
                     )
           AND :accountPersonUid IN 
               (SELECT ActorEntity.actorPersonUid
                  FROM ActorEntity
                 WHERE ActorEntity.actorUid = :actorUid)          
                     
    """)
    abstract suspend fun findResumableRegistration(
        activityUid: Long,
        accountPersonUid: Long,
        actorUid: Long,
    ): StatementEntity?


}