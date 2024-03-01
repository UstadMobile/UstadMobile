package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SELECT_CLAZZ_UID_FOR_ENROLMENT_UID_SQL
import com.ustadmobile.core.db.dao.ClazzEnrolmentDaoCommon.PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.CoursePermissionAndEnrolment
import com.ustadmobile.lib.db.composites.CoursePermissionAndListDetail
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
              ${CoursePermissionDaoCommon.LEFT_JOIN_ENROLMENT_FROM_COURSEPERMISSION_WITH_ACCOUNT_UID_PARAM}
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
            )
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
                  SELECT ${ClazzDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZENROLMENTUID_SQL_PT1} 
                         ($PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID)
                         ${ClazzDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2} 
                         ($PERMISSION_REQUIRED_BY_CLAZZENROLMENT_UID)
                         ${ClazzDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3} 
                )
               END 
    """)
    abstract suspend fun userHasEnrolmentEditPermission(
        accountPersonUid: Long,
        clazzEnrolmentUid: Long,
    ): Boolean


}