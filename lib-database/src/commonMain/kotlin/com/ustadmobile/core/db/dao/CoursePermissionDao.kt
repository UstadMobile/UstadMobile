package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_HAS_PERMISSION_WITH_CLAZZ_SQL
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.SELECT_COURSEPERMISSION_ENTITES_FOR_ACCOUNT_PERSON_UID_SQL
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.CoursePermissionAndEnrolment
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
import com.ustadmobile.lib.db.composites.PermissionPair
import com.ustadmobile.lib.db.composites.PermissionTriple
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CoursePermission
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class CoursePermissionDao {


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByClazzUidAsPagingSource",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT CoursePermission.*, Person.*, PersonPicture.*
          FROM CoursePermission
               LEFT JOIN Person
                         ON Person.personUid = CoursePermission.cpToPersonUid
               LEFT JOIN PersonPicture
                         ON PersonPicture.personPictureUid = Person.personUid
         WHERE CoursePermission.cpClazzUid = :clazzUid 
           AND (CAST(:includeDeleted AS INTEGER) = 1 OR NOT CoursePermission.cpIsDeleted) 
    """)
    abstract fun findByClazzUidAsPagingSource(
        clazzUid: Long,
        includeDeleted: Boolean,
    ): PagingSource<Int, CoursePermissionAndListDetail>


    /**
     * ClazzUid parameter is added because ViewModel will check permission on the same clazzuid
     */
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CoursePermission.*
          FROM CoursePermission
         WHERE CoursePermission.cpUid = :uid
           AND CoursePermission.cpClazzUid = :clazzUid
    """)
    abstract suspend fun findByUidAndClazzUid(uid: Long, clazzUid: Long): CoursePermission?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT CoursePermission.*
          FROM CoursePermission
         WHERE CoursePermission.cpUid = :uid
           AND CoursePermission.cpClazzUid = :clazzUid 
    """)
    abstract fun findByUidAndClazzUidAsFlow(uid: Long, clazzUid: Long): Flow<CoursePermission?>



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(coursePermission: CoursePermission)


    @Query("""
        UPDATE CoursePermission
           SET cpIsDeleted = :isDeleted,
               cpLastModified = :updateTime
         WHERE cpUid = :cpUid  
    """)
    abstract suspend fun setDeleted(cpUid: Long, isDeleted: Boolean, updateTime: Long)


    @Query("""
       SELECT CoursePermission.*, ClazzEnrolment_ForAccountPerson.*
         FROM CoursePermission
              ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FOR_ACCOUNT_PERSON_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
        WHERE CoursePermission.cpClazzUid = ($SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL)
          AND (CoursePermission.cpToPersonUid = :accountPersonUid 
               OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForAccountPerson.clazzEnrolmentRole)
    """)
    abstract suspend fun personHasPermissionWithClazzByEnrolmentUidEntities2(
        accountPersonUid: Long,
        clazzEnrolmentUid: Long,
    ): List<CoursePermissionAndEnrolment>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzByEnrolmentUidEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
        )
    )
    /**
     * Check if the current user has permission to edit/create an enrolment for a given course.
     *
     * If it is a new enrolment, then the user must have the system permission DIRECT_ENROL. If it
     * is an existing enrolment, the user must have the COURSE_MANAGE_STUDENT_ENROLMENT or
     * COURSE_MANAGE_TEACHER_ENROLMENT for the role of the given enrolmentUid.
     *
     * @param accountPersonUid the user to check for permissions
     * @param clazzEnrolmentUid the enrolment uid, if existing, else 0 (for new enrolment creation)
     */
    @Query("""
        SELECT CASE :clazzEnrolmentUid 
                WHEN 0 THEN (SELECT EXISTS(
                         SELECT 1
                           FROM SystemPermission
                          WHERE :accountPersonUid != 0 
                            AND SystemPermission.spToPersonUid = :accountPersonUid
                            AND (SystemPermission.spPermissionsFlag & ${PermissionFlags.DIRECT_ENROL}) > 0
                            AND NOT SystemPermission.spIsDeleted))
                ELSE (
                  SELECT ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZENROLMENTUID_SQL_PT1} 
                         ($PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID)
                         ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} 
                         ($PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID)
                         ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3} 
                )
               END 
    """)
    abstract suspend fun userHasEnrolmentEditPermission(
        accountPersonUid: Long,
        clazzEnrolmentUid: Long,
    ): Boolean


    @Query("""
       SELECT CoursePermission.*, ClazzEnrolment_ForAccountPerson.*
         FROM CoursePermission
              ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FOR_ACCOUNT_PERSON_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
        WHERE (:clazzUid = 0 OR CoursePermission.cpClazzUid = :clazzUid)
          AND (CoursePermission.cpToPersonUid = :accountPersonUid 
               OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForAccountPerson.clazzEnrolmentRole)
    """)
    abstract suspend fun personHasPermissionWithClazzEntities2(
        accountPersonUid: Long,
        clazzUid: Long,
    ): List<CoursePermissionAndEnrolment>


    /**
     * Determine if a person as per accountPersonUid has a particular permission on the given
     * clazz as per clazzUid
     *those
     * Note: when joining the coursepermission applicable for an enrolment role, there is only going
     * to be one CoursePermission entity per clazz and role combination. The join therefor does not
     * need to filter based on the permission.
     */
    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query(PERSON_HAS_PERMISSION_WITH_CLAZZ_SQL)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment"))
    abstract fun personHasPermissionWithClazzAsFlow2(
        accountPersonUid: Long,
        clazzUid: Long,
        permission: Long,
    ): Flow<Boolean>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS firstPermission,
               (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS secondPermission
    """)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment", "SystemPermission"))
    abstract fun personHasPermissionWithClazzPairAsFlow(
        accountPersonUid: Long,
        clazzUid: Long,
        firstPermission: Long,
        secondPermission: Long,
    ): Flow<PermissionPair>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS firstPermission,
               (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS secondPermission
    """)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment", "SystemPermission"))
    abstract suspend fun personHasPermissionWithClazzPairAsync(
        accountPersonUid: Long,
        clazzUid: Long,
        firstPermission: Long,
        secondPermission: Long,
    ): PermissionPair

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query("""
        SELECT (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :firstPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS firstPermission,
               (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :secondPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS secondPermission,
               (    (:clazzUid != 0 AND :accountPersonUid != 0)
                AND (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} :thirdPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} :thirdPermission
                     ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
               ) AS thirdPermission
    """)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment", "SystemPermission"))
    abstract fun personHasPermissionWithClazzTripleAsFlow(
        accountPersonUid: Long,
        clazzUid: Long,
        firstPermission: Long,
        secondPermission: Long,
        thirdPermission: Long,
    ): Flow<PermissionTriple>



    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
                functionDao = SystemPermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            )
        )
    )
    @Query(PERSON_HAS_PERMISSION_WITH_CLAZZ_SQL)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment"))
    abstract suspend fun personHasPermissionWithClazzAsync2(
        accountPersonUid: Long,
        clazzUid: Long,
        permission: Long,
    ): Boolean


    /**
     * Get all the CoursePermission entities that are applicable for a given user. This is useful
     * as part of Replicatable permission checks e.g. when checking if a person can view another
     * person, we need any course permissions that are granted to the given user based on their role
     * in a course (e.g. student or teacher) and those that are given to them directly.
     */
    @Query(SELECT_COURSEPERMISSION_ENTITES_FOR_ACCOUNT_PERSON_UID_SQL)
    abstract suspend fun findApplicableCoursePermissionEntitiesForAccountPerson(
        accountPersonUid: Long,
    ): List<CoursePermission>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            //Get the assignment entity itself
            HttpServerFunctionCall(
                functionName = "findByUidAndClazzUidAsync",
                functionDao = ClazzAssignmentDao::class,
            ),
            //Get the clazzenrolment for accountPersonUid
            HttpServerFunctionCall(
                functionName = "findByAccountPersonUidAndClazzUid",
                functionDao = ClazzEnrolmentDao::class,
            ),
            //Get the CoursePermission entities and any clazzenrolment related to those grants for
            //accountPersonUid
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2"
            ),
            //Get the system permissions for the given accountpersonuid
            HttpServerFunctionCall(
                functionName = "findAllByPersonUidEntities",
                functionDao = SystemPermissionDao::class,
            ),
            //Get CourseGroupMember for the current person uid (required if this is being used as part
            // of a peer marked assignment to grant permission)
            HttpServerFunctionCall(
                functionName = "findCourseGroupMembersByPersonUidAndAssignmentUid",
                functionDao = ClazzAssignmentDao::class
            ),
            //Get the peer reviewer allocations required for the current personuid
            HttpServerFunctionCall(
                functionName = "findPeerReviewerAllocationsByPersonUidAndAssignmentUid",
                functionDao = ClazzAssignmentDao::class,
            )

        )
    )
    /**
     * Determine what are the permissions available for accountPersonUid for the given assignment
     * and the given submitter.
     *
     * CanView: will be true when the accountPersonUid has learning record view permission for the
     * course, when the user is the submitter (either individually or as part of a group), or when
     * the user is allocated as a peer reviewer
     *
     * CanMark: will be true when the assignment is marked by teacher and the active user has the
     * learning record edit permission for the course, or when the assignment is marked by peers
     * and the accountPersonUid is selected to mark it.
     *
     */
    @Query("""
        WITH ${ClazzAssignmentDaoCommon.SELECT_SUBMITTER_UID_FOR_ACCOUNT_PERSON_UID_AND_ASSIGNMENT_CTE},
             CanMarkSubmitter(canMark) AS
             (SELECT CASE (SELECT ClazzAssignment.caMarkingType
                             FROM ClazzAssignment
                            WHERE ClazzAssignment.caUid = :assignmentUid)
                           WHEN ${ClazzAssignment.MARKED_BY_PEERS} THEN 
                                EXISTS(SELECT 1
                                         FROM PeerReviewerAllocation
                                        WHERE PeerReviewerAllocation.praToMarkerSubmitterUid = :submitterUid
                                          AND PeerReviewerAllocation.praMarkerSubmitterUid =
                                              (SELECT AccountSubmitterUid.accountSubmitterUid
                                                 FROM AccountSubmitterUid))
                           ELSE (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} ${PermissionFlags.COURSE_LEARNINGRECORD_EDIT}
                                 ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} ${PermissionFlags.COURSE_LEARNINGRECORD_EDIT}
                                 ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3})
                          END)

             
                    /* Can edit */
             SELECT (     (:accountPersonUid != 0 AND :assignmentUid != 0 AND :clazzUid != 0 AND :submitterUid != 0)
                      AND (SELECT CanMarkSubmitter.canMark 
                            FROM CanMarkSubmitter)) AS firstPermission,
                    /* can view */   
                    (     (:accountPersonUid != 0 AND :assignmentUid != 0 AND :clazzUid != 0 AND :submitterUid != 0)
                      AND (     (SELECT CanMarkSubmitter.canMark
                                   FROM CanMarkSubmitter)
                             OR (SELECT :submitterUid = 
                                  (SELECT AccountSubmitterUid.accountSubmitterUid
                                     FROM AccountSubmitterUid))
                             OR (${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1} ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                 ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                                 ${CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3}))               
                    ) AS secondPermission
             
    """)
    @QueryLiveTables(
        arrayOf(
            "ClazzAssignment", "ClazzEnrolment", "CourseGroupMember", "PeerReviewerAllocation",
            "CoursePermission", "SystemPermission"
        )
    )
    abstract fun userPermissionsForAssignmentSubmitterUid(
        accountPersonUid: Long,
        assignmentUid: Long,
        clazzUid: Long,
        submitterUid: Long,
    ): Flow<PermissionPair>



}