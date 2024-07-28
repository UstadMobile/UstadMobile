package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon.SELECT_SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
import com.ustadmobile.core.db.dao.SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.EditAndViewPermission
import com.ustadmobile.lib.db.composites.PermissionPair
import com.ustadmobile.lib.db.entities.SystemPermission
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class SystemPermissionDao {

    @Query("""
        SELECT SystemPermission.*
          FROM SystemPermission
         WHERE SystemPermission.spToPersonUid = :accountPersonUid
           AND (CAST(:includeDeleted AS INTEGER) = 1 OR NOT SystemPermission.spIsDeleted)
    """)
    abstract suspend fun findAllByPersonUid(
        accountPersonUid: Long,
        includeDeleted: Boolean
    ): List<SystemPermission>

    //This version avoids the need to specify includeDeleted when used for http entity pulling
    @Query("""
        SELECT SystemPermission.*
          FROM SystemPermission
         WHERE SystemPermission.spToPersonUid = :accountPersonUid
         """)
    abstract suspend fun findAllByPersonUidEntities(
        accountPersonUid: Long
    ): List<SystemPermission>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT SystemPermission.*
          FROM SystemPermission
         WHERE SystemPermission.spToPersonUid = :accountPersonUid
         LIMIT 1
    """)
    abstract fun findByPersonUidAsFlow(
        accountPersonUid: Long,
    ): Flow<SystemPermission?>


    @Query("""
        SELECT SystemPermission.*
          FROM SystemPermission
         WHERE SystemPermission.spToPersonUid = :accountPersonUid
         LIMIT 1
    """)
    abstract suspend fun findByPersonUid(
        accountPersonUid: Long,
    ): SystemPermission


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
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
    @Query(SELECT_SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL)
    abstract suspend fun personHasSystemPermission(
        accountPersonUid: Long,
        permission: Long,
    ): Boolean

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
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
    @Query(SELECT_SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL)
    abstract fun personHasSystemPermissionAsFlow(
        accountPersonUid: Long,
        permission: Long,
    ): Flow<Boolean>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
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
        SELECT ($SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
                :firstPermission
                $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2) as firstPermission,
                ($SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
                :secondPermission
                $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2) as secondPermission
    """)
    abstract fun personHasSystemPermissionPairAsFlow(
        accountPersonUid: Long,
        firstPermission: Long,
        secondPermission: Long,
    ): Flow<PermissionPair>




    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllByPersonUid",
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
        SELECT ($SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
                :firstPermission
                $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2) as firstPermission,
                ($SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1
                :secondPermission
                $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2) as secondPermission
    """)
    abstract suspend fun personHasSystemPermissionPair(
        accountPersonUid: Long,
        firstPermission: Long,
        secondPermission: Long,
    ): PermissionPair


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findApplicableCoursePermissionEntitiesForAccountPerson",
                functionDao = CoursePermissionDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findClazzEnrolmentEntitiesForPersonViewPermissionCheck",
                functionDao = ClazzEnrolmentDao::class,
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
    @Query("""
        /* Similar to the query used on list all persons, however it uses the the otherpersonuid param 
         * in the WHERE clauses to narrow down the search. 
         */
        WITH CanViewPersonUidsViaCoursePermission(personUid) AS
              /* Select personUids that can be viewed based on CoursePermission given to the active
               * user for their enrolment role in that course
              */
              (SELECT DISTINCT ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid AS personUid
                 FROM ClazzEnrolment ClazzEnrolment_ForActiveUser
                      JOIN CoursePermission 
                           ON CoursePermission.cpClazzUid = ClazzEnrolment_ForActiveUser.clazzEnrolmentClazzUid
                          AND CoursePermission.cpToEnrolmentRole = ClazzEnrolment_ForActiveUser.clazzEnrolmentRole
                          AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.PERSON_VIEW}) > 0
                      JOIN ClazzEnrolment ClazzEnrolment_ForClazzMember
                           ON ClazzEnrolment_ForClazzMember.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                              AND ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid = :otherPersonUid
                WHERE :accountPersonUid != 0
                  AND ClazzEnrolment_ForActiveUser.clazzEnrolmentPersonUid = :accountPersonUid
                  AND ClazzEnrolment_ForActiveUser.clazzEnrolmentActive
              
               UNION
               /* Select personUids that can be viewed based on CoursePermission for the active user
                  where the CoursePermission is granted directly to them
                */   
               SELECT DISTINCT ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid AS personUid
                 FROM CoursePermission
                      JOIN ClazzEnrolment ClazzEnrolment_ForClazzMember
                           ON ClazzEnrolment_ForClazzMember.clazzEnrolmentClazzUid = CoursePermission.cpClazzUid
                              AND ClazzEnrolment_ForClazzMember.clazzEnrolmentPersonUid = :otherPersonUid
                WHERE :accountPersonUid != 0
                  AND CoursePermission.cpToPersonUid = :accountPersonUid)
                  
        SELECT (    (SELECT :accountPersonUid = :otherPersonUid)
                 OR EXISTS(SELECT 1
                             FROM PersonParentJoin
                            WHERE PersonParentJoin.ppjMinorPersonUid = :otherPersonUid
                              AND PersonParentJoin.ppjParentPersonUid = :accountPersonUid)
                 OR (SELECT $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1 
                            ${PermissionFlags.PERSON_VIEW}
                            $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2)
                 OR (SELECT :otherPersonUid IN 
                             (SELECT CanViewPersonUidsViaCoursePermission.personUid
                                 FROM CanViewPersonUidsViaCoursePermission))           
               ) AS hasViewPermission,
               
               (   (SELECT :accountPersonUid = :otherPersonUid)
                OR EXISTS(SELECT 1
                             FROM PersonParentJoin
                            WHERE PersonParentJoin.ppjMinorPersonUid = :otherPersonUid
                              AND PersonParentJoin.ppjParentPersonUid = :accountPersonUid)
                OR (SELECT $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1 
                            ${PermissionFlags.EDIT_ALL_PERSONS}
                            $SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2)
                ) AS hasEditPermission
    """)
    abstract fun personHasEditAndViewPermissionForPersonAsFlow(
        accountPersonUid: Long,
        otherPersonUid: Long,
    ): Flow<EditAndViewPermission>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(systemPermissions: SystemPermission)

}