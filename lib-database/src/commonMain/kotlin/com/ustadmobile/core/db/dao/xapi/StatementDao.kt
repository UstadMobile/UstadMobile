package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.PERSON_UIDS_FOR_PAGED_GRADEBOOK_QUERY_CTE
import com.ustadmobile.core.db.dao.PersonDaoCommon.SORT_FIRST_NAME_ASC
import com.ustadmobile.core.db.dao.PersonDaoCommon.SORT_FIRST_NAME_DESC
import com.ustadmobile.core.db.dao.PersonDaoCommon.SORT_LAST_NAME_ASC
import com.ustadmobile.core.db.dao.PersonDaoCommon.SORT_LAST_NAME_DESC
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon
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
import com.ustadmobile.lib.db.composites.AttemptsPersonListConst
import com.ustadmobile.lib.db.composites.BlockStatus
import com.ustadmobile.lib.db.composites.PersonAndPictureAndNumAttempts
import com.ustadmobile.lib.db.composites.xapi.SessionTimeAndProgressInfo
import com.ustadmobile.lib.db.composites.xapi.StatementConst
import com.ustadmobile.lib.db.composites.xapi.StatementConst.SORT_BY_SCORE_ASC
import com.ustadmobile.lib.db.composites.xapi.StatementConst.SORT_BY_SCORE_DESC
import com.ustadmobile.lib.db.composites.xapi.StatementConst.SORT_BY_TIMESTAMP_ASC
import com.ustadmobile.lib.db.composites.xapi.StatementConst.SORT_BY_TIMESTAMP_DESC
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndRelated
import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.StatementEntityAndDisplayDetails
import com.ustadmobile.lib.db.entities.StatementReportData
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
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


    /**
     * Get StatementEntities required for findPersonsWithAttempts when running over http
     */
    @Query("""
        SELECT StatementEntity.*
          FROM StatementEntity
               LEFT JOIN ClazzEnrolment 
                         ON ClazzEnrolment.clazzEnrolmentUid =
                           COALESCE(
                            (SELECT ClazzEnrolment.clazzEnrolmentUid 
                               FROM ClazzEnrolment
                              WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                AND ClazzEnrolment.clazzEnrolmentActive
                                AND ClazzEnrolment.clazzEnrolmentClazzUid = StatementEntity.statementClazzUid 
                           ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                              LIMIT 1), 0)
         WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
           AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
           AND (    StatementEntity.statementActorPersonUid = :accountPersonUid
                      OR EXISTS(SELECT CoursePermission.cpUid
                                  FROM CoursePermission
                                 WHERE CoursePermission.cpClazzUid = StatementEntity.statementClazzUid
                                   AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                                        OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                                   AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                                   AND NOT CoursePermission.cpIsDeleted)
                      OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                          ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                          ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}))
                          
    """)
    abstract suspend fun findPersonsWithAttemptsStatements(
        contentEntryUid: Long,
        accountPersonUid: Long,
    ): List<StatementEntity>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findPersonsWithAttempts"),
            HttpServerFunctionCall("findPersonsWithAttemptsStatements")
        )
    )
    @Query("""
     SELECT Person.*, PersonPicture.*,
            (SELECT COUNT(*)
               FROM (SELECT DISTINCT StatementEntity.contextRegistrationHi, StatementEntity.contextRegistrationLo
                       FROM StatementEntity
                      WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                        AND StatementEntity.statementActorPersonUid = Person.personUid
                    ) AS DistinctRegistrations) AS numAttempts,
            (SELECT EXISTS(
                    SELECT 1
                      FROM StatementEntity
                     WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                       AND StatementEntity.statementActorPersonUid = Person.personUid
                       AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                       AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1)) AS isCompleted,
            (SELECT CASE
                    WHEN EXISTS(
                         SELECT 1
                           FROM StatementEntity
                          WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                            AND StatementEntity.statementActorPersonUid = Person.personUid
                            AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                            AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1) THEN 1
                    WHEN EXISTS(
                         SELECT 1
                           FROM StatementEntity
                          WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                            AND StatementEntity.statementActorPersonUid = Person.personUid
                            AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                            AND StatementEntity.resultSuccess IS NOT NULL
                            AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1) THEN 0
                    ELSE NULL
                    END) AS isSuccessful,
            (SELECT COALESCE(MAX(StatementEntity.extensionProgress), 0)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid
                AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                AND (StatementEntity.contextRegistrationHi, StatementEntity.contextRegistrationLo) IN (
                    SELECT s2.contextRegistrationHi, s2.contextRegistrationLo
                    FROM StatementEntity s2
                    WHERE s2.statementContentEntryUid = :contentEntryUid
                      AND s2.statementActorPersonUid = Person.personUid
                    ORDER BY s2.timestamp DESC
                    LIMIT 1
                )) AS maxProgress,
            (SELECT COALESCE(MAX(StatementEntity.resultScoreScaled), 0)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid
                AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1) AS maxScore,
            (SELECT MAX(StatementEntity.timestamp)
               FROM StatementEntity
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                AND StatementEntity.statementActorPersonUid = Person.personUid) AS mostRecentAttemptTime    
       FROM Person
            LEFT JOIN PersonPicture
                 ON PersonPicture.personPictureUid = Person.personUid
      WHERE Person.personUid IN
            (SELECT DISTINCT StatementEntity.statementActorPersonUid
               FROM StatementEntity
                    LEFT JOIN ClazzEnrolment 
                         ON ClazzEnrolment.clazzEnrolmentUid =
                           COALESCE(
                            (SELECT ClazzEnrolment.clazzEnrolmentUid 
                               FROM ClazzEnrolment
                              WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                AND ClazzEnrolment.clazzEnrolmentActive
                                AND ClazzEnrolment.clazzEnrolmentClazzUid = StatementEntity.statementClazzUid 
                           ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                              LIMIT 1), 0)
              WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                /* permission check */
                AND (    StatementEntity.statementActorPersonUid = :accountPersonUid
                      OR EXISTS(SELECT CoursePermission.cpUid
                                  FROM CoursePermission
                                 WHERE CoursePermission.cpClazzUid = StatementEntity.statementClazzUid
                                   AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                                        OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                                   AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                                   AND NOT CoursePermission.cpIsDeleted)
                      OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                          ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                          ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}))
            )      
            AND (:searchText = "%" OR Person.firstNames LIKE :searchText OR Person.lastName LIKE :searchText OR Person.userName LIKE :searchText)
     ORDER BY 
    CASE 
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_FIRST_NAME_ASC} THEN Person.firstNames
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_LAST_NAME_ASC} THEN Person.lastName
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_SCORE_ASC} THEN maxScore
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_COMPLETION_ASC} THEN maxProgress
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_RECENT_ATTEMPT_ASC} THEN mostRecentAttemptTime
        ELSE NULL
    END ASC,
    CASE 
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_FIRST_NAME_DESC} THEN Person.firstNames
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_LAST_NAME_DESC} THEN Person.lastName
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_SCORE_DESC} THEN maxScore
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_COMPLETION_DESC} THEN maxProgress
        WHEN :sortOrder = ${AttemptsPersonListConst.SORT_BY_RECENT_ATTEMPT_DESC} THEN mostRecentAttemptTime
        ELSE NULL
    END DESC


""")
    abstract fun findPersonsWithAttempts(
        contentEntryUid: Long,
        accountPersonUid: Long,
        searchText: String? = "%",
        sortOrder: Int,
        ): PagingSource<Int, PersonAndPictureAndNumAttempts>


    @Query("""
         WITH DistinctRegistrationUids(contextRegistrationHi, contextRegistrationLo, statementClazzUid) AS (
              SELECT DISTINCT StatementEntity.contextRegistrationHi, 
                             StatementEntity.contextRegistrationLo,
                             StatementEntity.statementClazzUid
                         FROM StatementEntity
                        WHERE StatementEntity.statementContentEntryUid = :contentEntryUid
                          AND StatementEntity.statementActorPersonUid = :personUid)
                        
       SELECT DistinctRegistrationUids.contextRegistrationHi AS contextRegistrationHi,
              DistinctRegistrationUids.contextRegistrationLo AS contextRegistrationLo,
              (SELECT MIN(StatementEntity.timestamp)
                 FROM StatementEntity
                WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                  AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                  AND StatementEntity.statementActorPersonUid = :personUid
                  AND StatementEntity.statementContentEntryUid = :contentEntryUid
              ) AS timeStarted,
                  (SELECT MAX(StatementEntity.extensionProgress)
                 FROM StatementEntity
                WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                  AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                  AND StatementEntity.statementActorPersonUid = :personUid
                  AND StatementEntity.statementContentEntryUid = :contentEntryUid
                  AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
               ) AS maxProgress,
              (SELECT MAX(StatementEntity.resultScoreScaled)
                 FROM StatementEntity
                WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                  AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                  AND StatementEntity.statementActorPersonUid = :personUid
                  AND StatementEntity.statementContentEntryUid = :contentEntryUid
                  AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
               ) AS maxScore,
              (SELECT EXISTS(
                      SELECT 1 
                        FROM StatementEntity
                       WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                         AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                         AND StatementEntity.statementActorPersonUid = :personUid
                         AND StatementEntity.statementContentEntryUid = :contentEntryUid
                         AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                         AND CAST(StatementEntity.resultCompletion AS INTEGER) = 1
              )) AS isCompleted,
              (SELECT CASE 
                      WHEN EXISTS(
                           SELECT 1 
                             FROM StatementEntity
                            WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                              AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                              AND StatementEntity.statementActorPersonUid = :personUid
                              AND StatementEntity.statementContentEntryUid = :contentEntryUid
                              AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                              AND CAST(StatementEntity.resultSuccess AS INTEGER) = 1) THEN 1
                      WHEN EXISTS(
                           SELECT 1 
                             FROM StatementEntity
                            WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                              AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                              AND StatementEntity.statementActorPersonUid = :personUid
                              AND StatementEntity.statementContentEntryUid = :contentEntryUid
                              AND CAST(StatementEntity.completionOrProgress AS INTEGER) = 1
                              AND StatementEntity.resultSuccess IS NOT NULL
                              AND CAST(StatementEntity.resultSuccess AS INTEGER) = 0) THEN 0
                      ELSE NULL
                      END) AS isSuccessful,
                      (SELECT MAX(StatementEntity.resultDuration)
                 FROM StatementEntity
                WHERE StatementEntity.contextRegistrationHi = DistinctRegistrationUids.contextRegistrationHi
                  AND StatementEntity.contextRegistrationLo = DistinctRegistrationUids.contextRegistrationLo
                  AND StatementEntity.statementActorPersonUid = :personUid
                  AND StatementEntity.statementContentEntryUid = :contentEntryUid
              ) AS resultDuration
         FROM DistinctRegistrationUids
         WHERE (    :personUid = :accountPersonUid 
                OR EXISTS(
                    SELECT CoursePermission.cpUid
                      FROM CoursePermission
                           LEFT JOIN ClazzEnrolment 
                                ON ClazzEnrolment.clazzEnrolmentUid =
                                  COALESCE(
                                   (SELECT ClazzEnrolment.clazzEnrolmentUid 
                                      FROM ClazzEnrolment
                                     WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                                       AND ClazzEnrolment.clazzEnrolmentActive
                                       AND ClazzEnrolment.clazzEnrolmentClazzUid = DistinctRegistrationUids.statementClazzUid 
                                  ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                                     LIMIT 1), 0)
                     WHERE CoursePermission.cpClazzUid = DistinctRegistrationUids.statementClazzUid
                       AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                            OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                       AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                       AND NOT CoursePermission.cpIsDeleted)
                OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                    ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                    ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}))      
         ORDER BY  
CASE :sortOrder
    WHEN 1 THEN timeStarted
    WHEN 8 THEN timeStarted
    ELSE NULL
END DESC,
CASE :sortOrder
    WHEN 2 THEN timeStarted
    WHEN 7 THEN timeStarted
    ELSE NULL
END ASC,
CASE :sortOrder
    WHEN 4 THEN maxScore
    ELSE NULL
END DESC,
CASE :sortOrder
    WHEN 3 THEN maxScore
    ELSE NULL
END ASC,
CASE :sortOrder
    WHEN 6 THEN maxProgress
    ELSE NULL
END DESC,
CASE :sortOrder
    WHEN 5 THEN maxProgress
    ELSE NULL
END ASC
         
   """)
    abstract fun findSessionsByPersonAndContent(
        contentEntryUid: Long,
        personUid: Long,
        accountPersonUid: Long,
        sortOrder: Int
    ): PagingSource<Int, SessionTimeAndProgressInfo>

    @HttpAccessible
    @Query("""
    SELECT StatementEntity.*, VerbEntity.*, VerbLangMapEntry.*
    FROM StatementEntity
    LEFT JOIN VerbEntity
        ON StatementEntity.statementVerbUid = VerbEntity.verbUid
    LEFT JOIN VerbLangMapEntry 
        ON (VerbLangMapEntry.vlmeVerbUid, VerbLangMapEntry.vlmeLangHash) = 
            (SELECT VerbLangMapEntry.vlmeVerbUid, VerbLangMapEntry.vlmeLangHash
            FROM VerbLangMapEntry
            WHERE VerbLangMapEntry.vlmeVerbUid = VerbEntity.verbUid
            ORDER BY VerbLangMapEntry.vlmeLastModified DESC
            LIMIT 1)
    LEFT JOIN ClazzEnrolment 
        ON ClazzEnrolment.clazzEnrolmentUid =
            COALESCE(
                (SELECT ClazzEnrolment.clazzEnrolmentUid 
                FROM ClazzEnrolment
                WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                    AND ClazzEnrolment.clazzEnrolmentActive
                    AND ClazzEnrolment.clazzEnrolmentClazzUid = StatementEntity.statementClazzUid 
                ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                LIMIT 1), 0)
    WHERE StatementEntity.contextRegistrationHi = :registrationHi
    AND StatementEntity.contextRegistrationLo = :registrationLo  
    AND StatementEntity.statementActorPersonUid = :selectedPersonUid
    AND StatementEntity.statementContentEntryUid = :contentEntryUid
    AND (:searchText = "%" OR VerbEntity.verbUrlId LIKE :searchText)
    AND (:selectedVerbsString = '' OR VerbEntity.verbUrlId IN 
        (SELECT word FROM 
            (WITH split(word, rest) AS (
                SELECT '', :selectedVerbsString || ','
                UNION ALL
                SELECT
                    substr(rest, 1, instr(rest, ',') - 1),
                    substr(rest, instr(rest, ',') + 1)
                FROM split
                WHERE rest <> ''
            )
            SELECT word FROM split WHERE word <> '')
        )
    )
    /* Filter out entries with no duration and duplicates */
    AND StatementEntity.resultDuration > 0  
    AND (
        StatementEntity.extensionProgress > 0  
        OR (
            CAST(StatementEntity.resultCompletion AS INTEGER) = 1  
            AND NOT EXISTS (
                SELECT 1 FROM StatementEntity s2 
                WHERE s2.contextRegistrationHi = StatementEntity.contextRegistrationHi
                AND s2.contextRegistrationLo = StatementEntity.contextRegistrationLo
                AND s2.statementActorPersonUid = StatementEntity.statementActorPersonUid
                AND CAST(s2.resultCompletion AS INTEGER) = 1
                AND s2.timestamp < StatementEntity.timestamp
            )
        )
    )
    /* Permission check for viewing user */
    AND (    :accountPersonUid = :selectedPersonUid 
          OR EXISTS(SELECT CoursePermission.cpUid
                      FROM CoursePermission
                     WHERE CoursePermission.cpClazzUid = StatementEntity.statementClazzUid
                       AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                            OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                       AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}) > 0 
                       AND NOT CoursePermission.cpIsDeleted)
          OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
              ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
              ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}))
    ORDER BY 
        CASE :sortOrder
            WHEN $SORT_BY_TIMESTAMP_DESC THEN StatementEntity.resultDuration
            ELSE NULL
        END DESC,
        CASE :sortOrder
            WHEN $SORT_BY_TIMESTAMP_ASC THEN StatementEntity.resultDuration
            ELSE NULL
        END ASC,
        CASE :sortOrder
            WHEN $SORT_BY_SCORE_DESC THEN StatementEntity.resultScoreRaw
            ELSE NULL
        END DESC,
        CASE :sortOrder
            WHEN $SORT_BY_SCORE_ASC THEN StatementEntity.resultScoreRaw
            ELSE NULL
        END ASC
""")
    abstract fun findStatementsBySession(
        registrationHi: Long,
        registrationLo: Long,
        accountPersonUid: Long,
        selectedPersonUid: Long,
        contentEntryUid: Long,
        searchText: String = "%",
        sortOrder: Int,
        selectedVerbsString: String = ""
    ): PagingSource<Int, StatementEntityAndVerb>

    @Query("""
    WITH DistinctVerbUrls(statementVerbUid) AS(
        SELECT DISTINCT StatementEntity.statementVerbUid
        FROM StatementEntity
        WHERE StatementEntity.contextRegistrationHi = :registrationHi
            AND StatementEntity.contextRegistrationLo = :registrationLo
            AND StatementEntity.statementActorPersonUid = :selectedPersonUid
            AND StatementEntity.statementContentEntryUid = :contentEntryUid
            /* Filter out entries with no progress/time */
            AND (StatementEntity.resultDuration > 0 
                 OR StatementEntity.extensionProgress > 0 
                 OR StatementEntity.resultScoreRaw IS NOT NULL
                 OR CAST(StatementEntity.resultCompletion AS INTEGER) = 1)
    )
    
    SELECT DistinctVerbUrls.statementVerbUid AS verbUid,
           VerbEntity.*,
           VerbLangMapEntry.*
    FROM DistinctVerbUrls
         LEFT JOIN VerbEntity 
                  ON VerbEntity.verbUid = DistinctVerbUrls.statementVerbUid
         LEFT JOIN VerbLangMapEntry
                  ON (VerbLangMapEntry.vlmeVerbUid, VerbLangMapEntry.vlmeLangHash) IN (
                     SELECT VerbLangMapEntry.vlmeVerbUid, VerbLangMapEntry.vlmeLangHash
                     FROM VerbLangMapEntry
                     WHERE VerbLangMapEntry.vlmeVerbUid = DistinctVerbUrls.statementVerbUid
                     LIMIT 1)
""")
    abstract fun getUniqueVerbsForSession(
        registrationHi: Long,
        registrationLo: Long,
        selectedPersonUid: Long,
        contentEntryUid: Long
    ): Flow<List<VerbEntity>>
    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM StatementEntity 
        WHERE statementContentEntryUid = :contentEntryUid 
        AND resultScoreScaled IS NOT NULL
    )
""")
    abstract suspend fun hasScoreData(contentEntryUid: Long): Boolean

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM StatementEntity 
        WHERE statementContentEntryUid = :contentEntryUid 
        AND extensionProgress IS NOT NULL
    )
""")
    abstract suspend fun hasCompletionData(contentEntryUid: Long): Boolean

}