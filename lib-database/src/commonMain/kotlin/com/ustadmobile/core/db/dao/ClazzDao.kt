package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SELECT_ACTIVE_CLAZZES
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SORT_ATTENDANCE_ASC
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SORT_ATTENDANCE_DESC
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SORT_CLAZZNAME_ASC
import com.ustadmobile.core.db.dao.ClazzDaoCommon.SORT_CLAZZNAME_DESC
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
import com.ustadmobile.core.db.dao.CoursePermissionDaoCommon.PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
import com.ustadmobile.lib.db.composites.ClazzAndDetailPermissions
import com.ustadmobile.lib.db.composites.ClazzNameAndTerminology
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_TEACHER
import com.ustadmobile.lib.db.entities.ClazzLog.Companion.STATUS_RECORDED

@Repository
@DoorDao
expect abstract class ClazzDao : BaseDao<Clazz> {


    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): Flow<Clazz?>

    @HttpAccessible(clientStrategy = HttpAccessible.ClientStrategy.HTTP_WITH_FALLBACK)
    @Query("SELECT * FROM Clazz WHERE clazzCode = :code")
    abstract suspend fun findByClazzCode(code: String): Clazz?

    @Query("SELECT * FROM Clazz WHERE clazzCode = :code")
    @Repository(Repository.METHOD_DELEGATE_TO_WEB)
    abstract suspend fun findByClazzCodeFromWeb(code: String): Clazz?

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAllLive(): Flow<List<Clazz>>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAll(): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzUid = :clazzUid")
    abstract suspend fun findByUidAsync(clazzUid: Long) : Clazz?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByUidAsync")
        )
    )
    @Query("""
        SELECT EXISTS(
               SELECT Clazz.clazzUid
                 FROM Clazz
                WHERE Clazz.clazzUid = :clazzUid)
    """)
    abstract suspend fun clazzUidExistsAsync(clazzUid: Long): Boolean


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUidAsFlow(uid: Long): Flow<Clazz?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT Clazz.*, 
               CoursePicture.*,
               HolidayCalendar.*, 
               CourseTerminology.*
          FROM Clazz 
               LEFT JOIN HolidayCalendar 
                         ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid

               LEFT JOIN CourseTerminology
                         ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
                      
               LEFT JOIN CoursePicture
                         ON CoursePicture.coursePictureUid = :uid
         WHERE Clazz.clazzUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): ClazzWithHolidayCalendarAndSchoolAndTerminology?

    @Update
    abstract suspend fun updateAsync(entity: Clazz): Int

    @HttpAccessible(
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findClazzesWithPermission"
            ),
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2",
                functionDao = CoursePermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "clazzUid",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0",
                    )
                )
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
        SELECT Clazz.*, ClazzEnrolment.*, CoursePicture.*,
               (SELECT COUNT(DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = $ROLE_STUDENT 
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                       AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numStudents,
               (SELECT COUNT(DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = $ROLE_TEACHER
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                        AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numTeachers,
               '' AS teacherNames,
               0 AS lastRecorded,
               CourseTerminology.*
          FROM Clazz
               LEFT JOIN ClazzEnrolment 
                    ON ClazzEnrolment.clazzEnrolmentUid =
                       COALESCE(
                       (SELECT ClazzEnrolment.clazzEnrolmentUid 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                           AND ClazzEnrolment.clazzEnrolmentActive
                           AND ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                      ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                         LIMIT 1), 0)
                LEFT JOIN CourseTerminology   
                          ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
                LEFT JOIN CoursePicture
                          ON CoursePicture.coursePictureUid = Clazz.clazzUid           

         WHERE /* Begin permission check clause */
               :accountPersonUid != 0
           AND (
                    Clazz.clazzOwnerPersonUid = :accountPersonUid
                 OR EXISTS(SELECT CoursePermission.cpUid
                             FROM CoursePermission
                            WHERE CoursePermission.cpClazzUid = Clazz.clazzUid
                              AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                                   OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                              AND (CoursePermission.cpPermissionsFlag & :permission) > 0 
                              AND NOT CoursePermission.cpIsDeleted)   
                 OR (${SystemPermissionDaoCommon.SELECT_SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL})             
                )
                /* End permission check clause */ 
           AND CAST(Clazz.isClazzActive AS INTEGER) = 1
           AND Clazz.clazzName like :searchQuery
           AND (Clazz.clazzUid NOT IN (:excludeSelectedClazzList))
           AND ( :filter = 0 OR (CASE WHEN :filter = $FILTER_CURRENTLY_ENROLLED 
                                      THEN :currentTime BETWEEN Clazz.clazzStartTime AND Clazz.clazzEndTime
                                      ELSE :currentTime > Clazz.clazzEndTime 
                                      END))
      GROUP BY Clazz.clazzUid, ClazzEnrolment.clazzEnrolmentUid, CourseTerminology.ctUid, CoursePicture.coursePictureUid
      ORDER BY CASE :sortOrder
               WHEN $SORT_ATTENDANCE_ASC THEN Clazz.attendanceAverage
               ELSE 0
               END ASC,
               CASE :sortOrder
               WHEN $SORT_CLAZZNAME_ASC THEN Clazz.clazzName
               ELSE ''
               END ASC,
               CASE :sortOrder
               WHEN $SORT_ATTENDANCE_DESC THEN Clazz.attendanceAverage
               ELSE 0
               END DESC,
               CASE :sortOrder
               WHEN $SORT_CLAZZNAME_DESC THEN clazz.Clazzname
               ELSE ''
               END DESC
    """)
    @QueryLiveTables(["Clazz", "ClazzEnrolment", "ScopedGrant", "PersonGroupMember","CourseTerminology"])
    abstract fun findClazzesWithPermission(
        searchQuery: String,
        accountPersonUid: Long,
        excludeSelectedClazzList: List<Long>,
        sortOrder: Int,
        filter: Int,
        currentTime: Long,
        permission: Long,
    ) : PagingSource<Int, ClazzWithListDisplayDetails>


    @Query("SELECT Clazz.clazzUid AS uid, Clazz.clazzName AS labelName From Clazz WHERE clazzUid IN (:ids)")
    abstract suspend fun getClassNamesFromListOfIds(ids: List<Long>): List<UidAndLabel>

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and CAST(isClazzActive AS INTEGER) = 1")
    abstract fun findByClazzName(name: String): List<Clazz>

    @Query("""
        UPDATE Clazz 
           SET attendanceAverage = 
               COALESCE(CAST(
                    (SELECT SUM(clazzLogNumPresent) 
                       FROM ClazzLog 
                      WHERE clazzLogClazzUid = :clazzUid
                       AND clazzLogStatusFlag = 4) AS REAL) /
                    
                    CAST(MAX(1.0, 
                        (SELECT SUM(clazzLogNumPresent) + SUM(clazzLogNumPartial) + SUM(clazzLogNumAbsent)
                        FROM ClazzLog 
                       WHERE clazzLogClazzUid = :clazzUid 
                        AND clazzLogStatusFlag = $STATUS_RECORDED)) AS REAL), 0),
               clazzLct = :timeChanged         
         WHERE clazzUid = :clazzUid
    """)
    @PostgresQuery("""
        UPDATE Clazz 
           SET attendanceAverage = 
               COALESCE(CAST(
                    (SELECT SUM(clazzLogNumPresent) 
                       FROM ClazzLog 
                      WHERE clazzLogClazzUid = :clazzUid
                       AND clazzLogStatusFlag = 4) AS REAL) /
                    
                    CAST(GREATEST(1.0, 
                        (SELECT SUM(clazzLogNumPresent) + SUM(clazzLogNumPartial) + SUM(clazzLogNumAbsent)
                        FROM ClazzLog 
                       WHERE clazzLogClazzUid = :clazzUid 
                        AND clazzLogStatusFlag = $STATUS_RECORDED)) AS REAL), 0),
               clazzLct = :timeChanged         
         WHERE clazzUid = :clazzUid
    """)
    abstract suspend fun updateClazzAttendanceAverageAsync(clazzUid: Long, timeChanged: Long)


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2",
                functionDao = CoursePermissionDao::class,
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
            HttpServerFunctionCall("clazzAndDetailPermissionsAsFlow"),
        )
    )
    @Query("""
        SELECT Clazz.*,
               (  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 
                  ${PermissionFlags.COURSE_ATTENDANCE_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
                  ${PermissionFlags.COURSE_ATTENDANCE_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
               ) AS hasAttendancePermission,
               (  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 
                  ${PermissionFlags.PERSON_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
                  ${PermissionFlags.PERSON_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
               ) AS hasViewMembersPermission,
               (  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 
                  ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
                  ${PermissionFlags.COURSE_LEARNINGRECORD_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
               ) AS hasLearningRecordPermission
          FROM Clazz
         WHERE Clazz.clazzUid = :clazzUid
           AND (  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT1 
                  ${PermissionFlags.COURSE_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT2
                  ${PermissionFlags.COURSE_VIEW}
                  $PERSON_COURSE_PERMISSION_CLAUSE_FOR_ACCOUNT_PERSON_UID_AND_CLAZZUID_SQL_PT3
               )
    """)
    @QueryLiveTables(arrayOf("Clazz", "CoursePermission", "ClazzEnrolment", "SystemPermission"))
    abstract fun clazzAndDetailPermissionsAsFlow(
        accountPersonUid: Long,
        clazzUid: Long,
    ): Flow<ClazzAndDetailPermissions?>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
        SELECT Clazz.*, 
               HolidayCalendar.*, 
               School.*,
               CoursePicture.*,
               (SELECT COUNT(DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = $ROLE_STUDENT 
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                        AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numStudents,
               (SELECT COUNT(DISTINCT ClazzEnrolment.clazzEnrolmentPersonUid) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = $ROLE_TEACHER 
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                       AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numTeachers,
                CourseTerminology.*      
         FROM Clazz 
              LEFT JOIN HolidayCalendar 
                        ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid
              LEFT JOIN School 
                        ON School.schoolUid = Clazz.clazzSchoolUid
              LEFT JOIN CourseTerminology
                        ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
              LEFT JOIN CoursePicture
                        ON CoursePicture.coursePictureUid = :clazzUid
        WHERE Clazz.clazzUid = :clazzUid""")
    abstract fun getClazzWithDisplayDetails(clazzUid: Long, currentTime: Long): Flow<ClazzWithDisplayDetails?>


    /**
     * Used for scheduling purposes - get a list of classes with the applicable holiday calendar.
     * This might be the holiday calendar specifeid by the class (if any) or the the calendar
     * specified for the associated school.
     */
    @Query("""
        SELECT Clazz.*, 
               HolidayCalendar.*, 
               School.*,
               CourseTerminology.*,
               CoursePicture.*
         FROM Clazz 
              LEFT JOIN HolidayCalendar 
              ON ((clazz.clazzHolidayUMCalendarUid != 0 
                AND HolidayCalendar.umCalendarUid = clazz.clazzHolidayUMCalendarUid)
                OR clazz.clazzHolidayUMCalendarUid = 0 AND clazz.clazzSchoolUid = 0 
                AND HolidayCalendar.umCalendarUid = (SELECT schoolHolidayCalendarUid 
                                                       FROM School 
                                                      WHERE schoolUid = clazz.clazzSchoolUid))
              LEFT JOIN School 
              ON School.schoolUid = Clazz.clazzSchoolUid
              
              LEFT JOIN CourseTerminology
              ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
              
              LEFT JOIN CoursePicture
                        ON CoursePicture.coursePictureUid = 0
                
        WHERE :filterUid = 0 
           OR Clazz.clazzUid = :filterUid
    """)
    abstract fun findClazzesWithEffectiveHolidayCalendarAndFilter(filterUid: Long): List<ClazzWithHolidayCalendarAndSchoolAndTerminology>

    @Query("SELECT Clazz.*, School.* FROM Clazz LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid WHERE clazz.clazzUid = :clazzUid")
    abstract suspend fun getClazzWithSchool(clazzUid: Long): ClazzWithSchool?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByUidAsync")
        ),
    )
    @Query("""
        SELECT Clazz.clazzName
          FROM Clazz
         WHERE Clazz.clazzUid = :clazzUid
    """)
    abstract fun getTitleByUidAsFlow(clazzUid: Long): Flow<String?>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("getClazzNameAndTerminologyAsFlow"),
            HttpServerFunctionCall("findByUidAsync")
        )
    )
    @Query("""
        SELECT Clazz.clazzName AS clazzName,
               CourseTerminology.*
          FROM Clazz
               LEFT JOIN CourseTerminology
                         ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
         WHERE Clazz.clazzUid = :clazzUid                
    """)
    abstract fun getClazzNameAndTerminologyAsFlow(clazzUid: Long): Flow<ClazzNameAndTerminology?>

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("getClazzNameAndTerminologyAsFlow"),
            HttpServerFunctionCall("findByUidAsync")
        )
    )
    @Query("""
        SELECT Clazz.clazzName AS clazzName
          FROM Clazz
         WHERE Clazz.clazzUid = :clazzUid                
    """)
    abstract fun getClazzNameAsFlow(clazzUid: Long): Flow<String?>


    @HttpAccessible(
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByUidAsync")
        ),
    )
    @Query("""
        SELECT Clazz.clazzTimeZone
          FROM Clazz
         WHERE Clazz.clazzUid = :clazzUid 
    """)
    abstract suspend fun getClazzTimeZoneByClazzUidAsync(clazzUid: Long): String?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
    )
    @Query("""
        SELECT Clazz.*
          FROM Clazz
         WHERE Clazz.clazzName IN (:names) 
    """)
    abstract suspend fun getCoursesByName(names: List<String>): List<Clazz>


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findOneRosterUserClazzes"),
            HttpServerFunctionCall(
                functionName = "personHasPermissionWithClazzEntities2",
                functionDao = CoursePermissionDao::class,
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "clazzUid",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0",
                    )
                )
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
     *
     * @param accountPersonUid the personuid for the auth token holder
     * @param filterByEnrolledMemberPersonUid the sourcedid of the user
     */
    @Query("""
        SELECT Clazz.*
          FROM CLAZZ
               LEFT JOIN ClazzEnrolment 
                    ON ClazzEnrolment.clazzEnrolmentUid =
                       COALESCE(
                       (SELECT ClazzEnrolment.clazzEnrolmentUid 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                           AND ClazzEnrolment.clazzEnrolmentActive
                           AND ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                      ORDER BY ClazzEnrolment.clazzEnrolmentDateLeft DESC   
                         LIMIT 1), 0)
          WHERE (   Clazz.clazzOwnerPersonUid = :accountPersonUid
                 OR EXISTS(SELECT CoursePermission.cpUid
                             FROM CoursePermission
                            WHERE CoursePermission.cpClazzUid = Clazz.clazzUid
                              AND (   CoursePermission.cpToPersonUid = :accountPersonUid 
                                   OR CoursePermission.cpToEnrolmentRole = ClazzEnrolment.clazzEnrolmentRole )
                              AND (CoursePermission.cpPermissionsFlag & ${PermissionFlags.COURSE_VIEW}) > 0 
                              AND NOT CoursePermission.cpIsDeleted)   
                 OR (${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT1}
                     ${PermissionFlags.COURSE_VIEW}
                     ${SystemPermissionDaoCommon.SYSTEM_PERMISSIONS_EXISTS_FOR_ACCOUNTUID_SQL_PT2}
                    )             
                )
           AND EXISTS 
                (SELECT ClazzEnrolment.clazzEnrolmentUid
                   FROM ClazzEnrolment
                  WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :filterByEnrolledMemberPersonUid
                    AND ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid
                )  
    """)
    abstract suspend fun findOneRosterUserClazzes(
        accountPersonUid: Long,
        filterByEnrolledMemberPersonUid: Long,
    ): List<Clazz>


}
