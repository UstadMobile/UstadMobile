package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT
import com.ustadmobile.door.DoorQuery
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.BlockStatus
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

    //To pull over http - change these to selecting the statementuid(s)
    @Query("""
        SELECT Person.personUid AS sPersonUid,
               CourseBlock.cbUid AS sCbUid,
               (SELECT MAX(StatementEntity.extensionProgress)
                  FROM StatementEntity
                       JOIN ActorEntity
                            ON StatementEntity.statementActorUid = ActorEntity.actorUid
                 WHERE StatementEntity.statementCbUid = CourseBlock.cbUid
                   AND ActorEntity.actorAccountName = Person.username 
               ) AS sProgress,
               FALSE AS sIsCompleted,
               NULL AS sIsSuccess,
               1 AS sScoreScaled
          FROM Person
               JOIN CourseBlock
                    ON CourseBlock.cbClazzUid = :clazzUid
         WHERE Person.personUid IN (:studentPersonUids)           
    """)
    abstract suspend fun findStatusForStudentsInClazz(
        clazzUid: Long,
        studentPersonUids: List<Long>,
    ): List<BlockStatus>


}