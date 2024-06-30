package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.annotation.*
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
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.SELECT_COURSEPERMISSION_ENTITES_FOR_ACCOUNT_PERSON_UID_SQL
import com.ustadmobile.core.db.dao.xapi.StatementDao
import com.ustadmobile.lib.db.composites.ClazzEnrolmentAndPerson
import com.ustadmobile.lib.db.composites.ClazzEnrolmentAndPersonDetailDetails
import com.ustadmobile.lib.db.composites.CourseNameAndPersonName
import com.ustadmobile.lib.db.composites.PersonAndClazzMemberListDetails
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import kotlinx.coroutines.flow.Flow

@Repository
@DoorDao
expect abstract class ClazzEnrolmentDao : BaseDao<ClazzEnrolment> {

    /**
     * Note: When actually enroling into a class, use UmAppDatbaseExt#processEnrolmentIntoClass
     * to ensure that permissions, group membership, etc. are taken care of
     */
    @Insert
    abstract fun insertListAsync(entityList: List<ClazzEnrolment>)

    @Query("""
        SELECT ClazzEnrolment.*, LeavingReason.*, 
               COALESCE(Clazz.clazzTimeZone, COALESCE(School.schoolTimeZone, 'UTC')) as timeZone
          FROM ClazzEnrolment 
               LEFT JOIN LeavingReason 
                         ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
               LEFT JOIN Clazz 
                         ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
               LEFT JOIN School 
                         ON School.schoolUid = Clazz.clazzSchoolUid
         WHERE clazzEnrolmentPersonUid = :personUid 
           AND ClazzEnrolment.clazzEnrolmentActive 
           AND clazzEnrolmentClazzUid = :clazzUid 
      ORDER BY clazzEnrolmentDateLeft DESC
           """)
    abstract fun findAllEnrolmentsByPersonAndClazzUid(
        personUid: Long,
        clazzUid: Long
    ): Flow<List<ClazzEnrolmentWithLeavingReason>>


    @HttpAccessible
    @Query("""
            SELECT ClazzEnrolment.*, 
                   LeavingReason.*,
                   COALESCE(Clazz.clazzTimeZone, COALESCE(School.schoolTimeZone, 'UTC')) AS timeZone
              FROM ClazzEnrolment 
                   LEFT JOIN LeavingReason 
                             ON LeavingReason.leavingReasonUid = ClazzEnrolment.clazzEnrolmentLeavingReasonUid
                   LEFT JOIN Clazz 
                             ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                   LEFT JOIN School 
                             ON School.schoolUid = Clazz.clazzSchoolUid
             WHERE ClazzEnrolment.clazzEnrolmentUid = :enrolmentUid
             """)
    abstract suspend fun findEnrolmentWithLeavingReason(
        enrolmentUid: Long
    ): ClazzEnrolmentWithLeavingReason?

    @Query("""
        UPDATE ClazzEnrolment 
          SET clazzEnrolmentDateLeft = :endDate,
              clazzEnrolmentLct = :updateTime
        WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract suspend fun updateDateLeftByUid(clazzEnrolmentUid: Long, endDate: Long, updateTime: Long)

    @Update
    abstract suspend fun updateAsync(entity: ClazzEnrolment): Int

    @Query("""
               /* List of all CoursePermissions that are granted to the person as per accountPersonUid */
          WITH CoursePermissionsForAccountPerson AS (
               $SELECT_COURSEPERMISSION_ENTITES_FOR_ACCOUNT_PERSON_UID_SQL),
               /* Check if CoursePermission for accountPersonUid grants view permission */
               CanViewPersonUidViaCoursePermission(personUid) AS (
                    SELECT ClazzEnrolment.clazzEnrolmentPersonUid
                      FROM CoursePermissionsForAccountPerson
                           JOIN ClazzEnrolment 
                                ON (CoursePermissionsForAccountPerson.cpPermissionsFlag & ${PermissionFlags.PERSON_VIEW}) > 0
                               AND ClazzEnrolment.clazzEnrolmentClazzUid = CoursePermissionsForAccountPerson.cpClazzUid  
                     WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :otherPersonUid         
               )     
        SELECT ClazzEnrolment.*,
               Clazz.*,
               CourseTerminology.*
          FROM ClazzEnrolment
               JOIN Clazz 
                    ON Clazz.clazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
               LEFT JOIN CourseTerminology
                    ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
         WHERE (:accountPersonUid != 0 AND :otherPersonUid != 0)
           AND ClazzEnrolment.clazzEnrolmentPersonUid = :otherPersonUid
               /* Check that accountPersonUid has permission to see otherPerson */
           AND (    (SELECT :accountPersonUid = :otherPersonUid)
                 OR (SELECT ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1} 
                            ${PermissionFlags.PERSON_VIEW}
                            ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2})
                 OR (SELECT :otherPersonUid IN 
                             (SELECT CanViewPersonUidViaCoursePermission.personUid
                                 FROM CanViewPersonUidViaCoursePermission))           
               ) 
              /* Check that accountPersonUid has permission to see related Clazz */
          AND (     (SELECT :accountPersonUid = :otherPersonUid)
                 OR (SELECT ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1} 
                            ${PermissionFlags.COURSE_VIEW}
                            ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2})
                 OR (EXISTS(SELECT 1
                              FROM CoursePermissionsForAccountPerson
                             WHERE CoursePermissionsForAccountPerson.cpClazzUid = ClazzEnrolment.clazzEnrolmentClazzUid
                               AND (CoursePermissionsForAccountPerson.cpPermissionsFlag & ${PermissionFlags.COURSE_VIEW}) > 0)) 
               )
    """)
    /**
     * Provide a list of the clazzes a given person is in with the class information itself (e.g.
     * for use to show the enrolment list on PersonDetailViewModel). If the accountPersonUid does
     * not have view permission for otherPersonUid, the list returned will be empty. The list
     * returned will only include enrolments where the person has the view_course permission for the
     * related Clazz.
     *
     * @param accountPersonUid the personuid of the currently active account (used for permission checks)
     * @param otherPersonUid the personUid to get an enrolment list for
     */
    abstract fun findAllClazzesByPersonWithClazz(
        accountPersonUid: Long,
        otherPersonUid: Long
    ): Flow<List<ClazzEnrolmentAndPersonDetailDetails>>

    /**
     * Simple query for internal tests etc
     */
    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid 
    """)
    abstract fun findAllByPersonUid(personUid: Long): Flow<List<ClazzEnrolment>>



    @Query("""SELECT ClazzEnrolment.*, Clazz.* 
        FROM ClazzEnrolment 
        LEFT JOIN Clazz ON ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
        WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid 
        AND ClazzEnrolment.clazzEnrolmentActive
        ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC
    """)
    abstract suspend fun findAllClazzesByPersonWithClazzAsListAsync(personUid: Long): List<ClazzEnrolmentWithClazz>

    @Query("""
        SELECT ClazzEnrolment.*, Person.*
          FROM ClazzEnrolment
                LEFT JOIN Person 
                          ON ClazzEnrolment.clazzEnrolmentPersonUid = Person.personUid
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
              AND :date BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
              AND ClazzEnrolment.clazzEnrolmentDateLeft
              AND CAST(clazzEnrolmentActive AS INTEGER) = 1
              AND (:roleFilter = 0 OR ClazzEnrolment.clazzEnrolmentRole = :roleFilter)
              AND (:personUidFilter = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :personUidFilter)
    """)
    abstract suspend fun getAllClazzEnrolledAtTimeAsync(
        clazzUid: Long,
        date: Long,
        roleFilter: Int,
        personUidFilter: Long = 0
    ): List<ClazzEnrolmentWithPerson>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
           AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
           AND :time BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                         AND ClazzEnrolment.clazzEnrolmentDateLeft
           AND ClazzEnrolment.clazzEnrolmentActive              
    """)
    abstract suspend fun getAllEnrolmentsAtTimeByClazzAndPerson(
        clazzUid: Long,
        accountPersonUid: Long,
        time: Long,
    ): List<ClazzEnrolment>


    @Query("SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentUid = :uid")
    abstract suspend fun findByUid(uid: Long): ClazzEnrolment?

    @Query("SELECT * FROM ClazzEnrolment WHERE clazzEnrolmentUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<ClazzEnrolment?>


    @Query(ClazzEnrolmentDaoCommon.SELECT_BY_UID_AND_ROLE_SQL)
    @QueryLiveTables(value = ["Clazz", "Person", "ClazzEnrolment", "PersonPicture", "CoursePermission"])
    @HttpAccessible(
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByClazzUidAndRole"),
            HttpServerFunctionCall("findEnrolmentsByClazzUidAndRole"),
        )
    )
    abstract fun findByClazzUidAndRole(
        clazzUid: Long,
        roleId: Int,
        sortOrder: Int,
        searchText: String? = "%",
        filter: Int,
        accountPersonUid: Long,
        currentTime: Long,
        permission: Long,
    ): PagingSource<Int, PersonAndClazzMemberListDetails>


    /**
     * This is effectively the same query as above, however needs to trigger additional
     * http server function calls
     */
    @Query("""
        SELECT * 
          FROM (SELECT Person.*, PersonPicture.*,
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
                       LEFT JOIN PersonPicture
                                 ON PersonPicture.personPictureUid = Person.personUid
                       --Dummy join so that invalidations of the StatementEntity table will trigger
                       -- PagingSource invalidation of ClazzGradebookPagingSource
                       LEFT JOIN StatementEntity
                                 ON StatementEntity.statementIdHi = 0 
                                    AND StatementEntity.statementIdLo = 0
                       LEFT JOIN ActorEntity
                                 ON ActorEntity.actorUid = 0
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
                           ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL)
                        OR Person.personUid = :accountPersonUid
                       )  
                   /* End permission check */                   
                   AND Person.firstNames || ' ' || Person.lastName LIKE :searchText
               GROUP BY Person.personUid, PersonPicture.personPictureUid) AS CourseMember
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
    """)
    @QueryLiveTables(
        value = [
            "Clazz", "Person", "ClazzEnrolment", "PersonPicture",
            "CoursePermission", "StatementEntity", "ActorEntity"
        ]
    )
    @HttpAccessible(
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByClazzUidAndRoleForGradebook"),
            HttpServerFunctionCall("findEnrolmentsByClazzUidAndRole"),
            HttpServerFunctionCall(
                functionName = "findStatusForStudentsInClazzStatements",
                functionDao = StatementDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findActorEntitiesForGradebook"
            )
        )
    )
    abstract fun findByClazzUidAndRoleForGradebook(
        clazzUid: Long,
        roleId: Int,
        sortOrder: Int,
        searchText: String? = "%",
        filter: Int,
        accountPersonUid: Long,
        currentTime: Long,
        permission: Long,
    ): PagingSource<Int, PersonAndClazzMemberListDetails>

    @Query("""
        SELECT ActorEntity.*
          FROM ActorEntity
         WHERE ActorEntity.actorPersonUid IN 
               (SELECT DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid 
                   AND ClazzEnrolment.clazzEnrolmentActive 
                   AND ClazzEnrolment.clazzEnrolmentRole = ${ClazzEnrolment.ROLE_STUDENT})
    """)
    abstract suspend fun findActorEntitiesForGradebook(
        clazzUid: Long,
    ): List<ActorEntity>

    /**
     * Get a list of all enrolments with associated person entity that the given accountpersonuid
     * can access.
     */
    @Query("""
       SELECT ClazzEnrolment.*,
              Person.*,
              PersonPicture.*
         FROM ClazzEnrolment
              JOIN Person
                   ON Person.personUid = ClazzEnrolment.clazzEnrolmentPersonUid
              LEFT JOIN PersonPicture
                   ON PersonPicture.personPictureUid = ClazzEnrolment.clazzEnrolmentPersonUid
                   
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
              /* Begin permission check*/
          AND (
                   (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1}
                    ${PermissionFlags.PERSON_VIEW}
                    ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2}
                    ${PermissionFlags.PERSON_VIEW}
                    ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
              )  
              /* End permission check */
    """)
    abstract suspend fun findEnrolmentsAndPersonByClazzUidWithPermissionCheck(
        clazzUid: Long,
        accountPersonUid: Long,
    ): List<ClazzEnrolmentAndPerson>


    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
           AND ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
    """)
    abstract suspend fun findByAccountPersonUidAndClazzUid(
        accountPersonUid: Long,
        clazzUid: Long,
    ): List<ClazzEnrolment>


    @Query("""
       SELECT ClazzEnrolment.*
         FROM ClazzEnrolment
        WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
          AND ClazzEnrolment.clazzEnrolmentRole = :roleId
              /* Begin permission check*/
          AND (
                   ($PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL)
                OR ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
              )  
              /* End permission check */
    """)
    abstract suspend fun findEnrolmentsByClazzUidAndRole(
        clazzUid: Long,
        accountPersonUid: Long,
        roleId: Int,
        permission: Long,
    ): List<ClazzEnrolment>



    @Query("""
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentClazzUid = :clazzUid
           AND ClazzEnrolment.clazzEnrolmentRole = :roleId
    """)
    abstract suspend fun findAllEnrolmentsByClazzUidAndRole(
        clazzUid: Long,
        roleId: Int
    ): List<ClazzEnrolment>

    @Query("""
        UPDATE ClazzEnrolment 
          SET clazzEnrolmentActive = :enrolled,
              clazzEnrolmentLct = :timeChanged
        WHERE clazzEnrolmentUid = :clazzEnrolmentUid""")
    abstract fun updateClazzEnrolmentActiveForClazzEnrolment(
        clazzEnrolmentUid: Long,
        enrolled: Boolean,
        timeChanged: Long,
    ): Int

    @Query("""
            UPDATE ClazzEnrolment 
               SET clazzEnrolmentRole = :newRole,
                   clazzEnrolmentLct = :updateTime      
             -- Avoid potential for duplicate approvals if user was previously refused      
             WHERE clazzEnrolmentUid = COALESCE( 
                    (SELECT clazzEnrolmentUid
                       FROM ClazzEnrolment
                      WHERE clazzEnrolmentPersonUid = :personUid 
                            AND clazzEnrolmentClazzUid = :clazzUid
                            AND clazzEnrolmentRole = :oldRole
                            AND CAST(clazzEnrolmentActive AS INTEGER) = 1
                      LIMIT 1), 0)""")
    abstract suspend fun updateClazzEnrolmentRole(
        personUid: Long,
        clazzUid: Long,
        newRole: Int,
        oldRole: Int,
        updateTime: Long
    ): Int

    @Query("""
        SELECT Person.firstNames, Person.lastName, Clazz.clazzName
          FROM Person
               LEFT JOIN Clazz
                         ON Clazz.clazzUid = :clazzUid
        WHERE Person.personUid = :personUid                 
    """)
    abstract suspend fun getClazzNameAndPersonName(
        personUid: Long,
        clazzUid: Long,
    ): CourseNameAndPersonName?


    /**
     * Find the enrolments required for a given accountPersonUid to check their permissions to view
     * another person, optionally filtered by person.
     *
     * This will include the ClazzEnrolment(s) of accountPersonUid themselves and all ClazzEnrolment
     * entities for any Clazz where they have permission to view members.
     *
     * @param accountPersonUid the active user for whom we are checking permissions
     * @param otherPersonUid the person that we want to check if accountPersonUid has permission to
     *        view when checking for a specific person, or 0 if fetching all (e.g. listing all persons)
     */
    @Query("""
          WITH CanViewMembersClazzesViaCoursePermission(clazzUid) AS
               /* Get clazzuids where active user can view members based on their own enrolment role */
               (SELECT CoursePermission.cpClazzUid
                  FROM ClazzEnrolment ClazzEnrolment_ActiveUser
                       JOIN CoursePermission 
                            ON CoursePermission.cpClazzUid = ClazzEnrolment_ActiveUser.clazzEnrolmentClazzUid
                           AND CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ActiveUser.clazzEnrolmentRole
                 WHERE ClazzEnrolment_ActiveUser.clazzEnrolmentPersonUid = :accountPersonUid 
                   AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.PERSON_VIEW}) > 0 
                UNION
                /* Get ClazzUids where the active user can view members based a grant directly to them */
                SELECT CoursePermission.cpClazzUid
                  FROM CoursePermission
                 WHERE CoursePermission.cpToPersonUid  = :accountPersonUid
                   AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.PERSON_VIEW}) > 0
               )
        SELECT ClazzEnrolment.*
          FROM ClazzEnrolment
         WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
            OR (    ClazzEnrolment.clazzEnrolmentClazzUid IN 
                        (SELECT CanViewMembersClazzesViaCoursePermission.clazzUid
                           FROM CanViewMembersClazzesViaCoursePermission)
                AND (:otherPersonUid = 0 OR ClazzEnrolment.clazzEnrolmentPersonUid = :otherPersonUid)   
                )
    """)
    abstract suspend fun findClazzEnrolmentEntitiesForPersonViewPermissionCheck(
        accountPersonUid: Long,
        otherPersonUid: Long,
    ): List<ClazzEnrolment>

}
