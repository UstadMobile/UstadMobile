package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.FILTER_ACTIVE_ONLY
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_ASC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_DATE_LEFT_DESC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_ASC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_DATE_REGISTERED_DESC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.SORT_LAST_NAME_DESC
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.ACTOR_UIDS_FOR_PERSONUIDS_CTE
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CLAZZ_STUDENT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_STATUS_STATEMENTS_FOR_CONTENT_ENTRY
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.FROM_STATEMENT_ENTITY_WHERE_MATCHES_ACCOUNT_PERSON_UID_AND_PARENT_CONTENT_ENTRY_ROOT
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID_INNER
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS
import com.ustadmobile.core.db.dao.xapi.StatementDaoCommon.STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS_INNER
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
import com.ustadmobile.lib.db.entities.xapi.XapiEntityObjectTypeFlags
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
        WITH PersonUids(personUid) AS (
            SELECT CourseMember.personUid 
              FROM (SELECT Person.*,
                           (SELECT MIN(ClazzEnrolment.clazzEnrolmentDateJoined) 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS earliestJoinDate, 
            
                           (SELECT MAX(ClazzEnrolment.clazzEnrolmentDateLeft) 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid) AS latestDateLeft, 
            
                           (SELECT ClazzEnrolment.clazzEnrolmentRole 
                              FROM ClazzEnrolment 
                             WHERE Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid 
                               AND ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                               AND ClazzEnrolment.clazzEnrolmentActive
                          ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
                             LIMIT 1) AS enrolmentRole
                      FROM Person
                     WHERE Person.personUid IN 
                           (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid 
                              FROM ClazzEnrolment 
                             WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                               AND ClazzEnrolment.clazzEnrolmentActive 
                               AND ClazzEnrolment.clazzEnrolmentRole = :roleId 
                               AND (:filter != $FILTER_ACTIVE_ONLY 
                                     OR (:currentTime 
                                          BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                                          AND ClazzEnrolment.clazzEnrolmentDateLeft))) 
                       /* Begin permission check */
                       AND (
                               ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2 ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3)
                            OR Person.personUid = :accountPersonUid
                           )  
                       /* End permission check */                   
                       AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
                   GROUP BY Person.personUid) AS CourseMember
          ORDER BY CASE(:sortOrder)
                    WHEN $SORT_FIRST_NAME_ASC THEN CourseMember.firstNames
                    WHEN $SORT_LAST_NAME_ASC THEN CourseMember.lastName
                    ELSE ''
                END ASC,
                CASE(:sortOrder)
                    WHEN $SORT_FIRST_NAME_DESC THEN CourseMember.firstNames
                    WHEN $SORT_LAST_NAME_DESC THEN CourseMember.lastName
                    ELSE ''
                END DESC,
                CASE(:sortOrder)
                    WHEN $SORT_DATE_REGISTERED_ASC THEN CourseMember.earliestJoinDate
                    WHEN $SORT_DATE_LEFT_ASC THEN CourseMember.latestDateLeft
                    ELSE 0
                END ASC,
                CASE(:sortOrder)
                    WHEN $SORT_DATE_REGISTERED_DESC THEN CourseMember.earliestJoinDate
                    WHEN $SORT_DATE_LEFT_DESC THEN CourseMember.latestDateLeft
                    ELSE 0
                END DESC
             LIMIT :studentsLimit
            OFFSET :studentsOffset   
         ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE

        -- Fetch all statements that could be completion or progress for the Gradebook report
        SELECT StatementEntity.*, ActorEntity.*, GroupMemberActorJoin.*
          FROM StatementEntity
               JOIN ActorEntity
                    ON ActorEntity.actorUid = StatementEntity.statementActorUid
               LEFT JOIN GroupMemberActorJoin
                    ON ActorEntity.actorObjectType = ${XapiEntityObjectTypeFlags.GROUP}
                       AND GroupMemberActorJoin.gmajGroupActorUid = StatementEntity.statementActorUid
                       AND GroupMemberActorJoin.gmajMemberActorUid IN (
                           SELECT DISTINCT ActorUidsForPersonUid.actorUid
                             FROM ActorUidsForPersonUid)
         WHERE StatementEntity.statementClazzUid = :clazzUid
           AND StatementEntity.completionOrProgress = :completionOrProgressTrueVal
           AND StatementEntity.statementActorUid IN (
               SELECT DISTINCT ActorUidsForPersonUid.actorUid
                 FROM ActorUidsForPersonUid) 
           AND (      StatementEntity.resultScoreScaled IS NOT NULL
                   OR StatementEntity.resultCompletion IS NOT NULL
                   OR StatementEntity.resultSuccess IS NOT NULL
                   OR StatementEntity.extensionProgress IS NOT NULL 
               )
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


    @Query("""
        WITH PersonUids(personUid) AS (
             SELECT Person.personUid
               FROM Person
              WHERE Person.personUid IN (:studentPersonUids)
        ),
        
        $ACTOR_UIDS_FOR_PERSONUIDS_CTE,
        
        PersonUidsAndCourseBlocks(personUid, cbUid, cbType, caMarkingType) AS (
             SELECT Person.personUid AS personUid,
                    CourseBlock.cbUid AS cbUid,
                    CourseBlock.cbType AS cbType,
                    ClazzAssignment.caMarkingType AS caMarkingType
               FROM Person
                    JOIN CourseBlock
                         ON CourseBlock.cbClazzUid = :clazzUid
                    LEFT JOIN ClazzAssignment
                         ON CourseBlock.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                        AND ClazzAssignment.caUid = CourseBlock.cbEntityUid     
              WHERE Person.personUid IN (:studentPersonUids)       
        )
        
        SELECT PersonUidsAndCourseBlocks.personUid AS sPersonUid,
               PersonUidsAndCourseBlocks.cbUid AS sCbUid,
               (SELECT MAX(StatementEntity.extensionProgress)
                  FROM StatementEntity
                       $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                 WHERE $STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS 
               ) AS sProgress,
               (SELECT EXISTS(
                       SELECT 1
                         FROM StatementEntity
                              $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                        WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
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
                                           $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                    WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
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
                                           $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                    WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
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
                       WHEN (     PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                              AND PersonUidsAndCourseBlocks.caMarkingType = ${ClazzAssignment.MARKED_BY_PEERS}
                            ) 
                            THEN (SELECT AVG(StatementEntity.resultScoreScaled)
                                    FROM StatementEntity
                                         $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                   WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                     AND StatementEntity.timestamp = (
                                         SELECT MAX(StatementEntity_Inner.timestamp)
                                           FROM StatementEntity StatementEntity_Inner
                                                $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID_INNER
                                          WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS_INNER)
                                            AND StatementEntity_Inner.contextInstructorActorUid = StatementEntity.contextInstructorActorUid)
                                   LIMIT 1)
                       -- When an assignment, but not peer marked, then the latest score     
                       WHEN PersonUidsAndCourseBlocks.cbType = ${CourseBlock.BLOCK_ASSIGNMENT_TYPE}
                            THEN (SELECT StatementEntity.resultScoreScaled
                                    FROM StatementEntity
                                         $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                                   WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS)
                                ORDER BY StatementEntity.timestamp DESC
                                   LIMIT 1)
                       -- else the best score accomplished so far            
                       ELSE (SELECT MAX(StatementEntity.resultScoreScaled) 
                               FROM StatementEntity
                                    $JOIN_ACTOR_TABLES_FROM_ACTOR_UIDS_FOR_PERSON_UID
                              WHERE ($STATEMENT_MATCHES_PERSONUIDS_AND_COURSEBLOCKS))            
                       END
               ) AS sScoreScaled
          FROM PersonUidsAndCourseBlocks
    """)
    abstract suspend fun findStatusForStudentsInClazz(
        clazzUid: Long,
        studentPersonUids: List<Long>,
    ): List<BlockStatus>


}