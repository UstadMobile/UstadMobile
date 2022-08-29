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
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1
import com.ustadmobile.lib.db.entities.Clazz.Companion.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_STUDENT
import com.ustadmobile.lib.db.entities.ClazzEnrolment.Companion.ROLE_TEACHER
import com.ustadmobile.lib.db.entities.ClazzLog.Companion.STATUS_RECORDED

@Repository
@DoorDao
expect abstract class ClazzDao : BaseDao<Clazz> {

    @Query("""
     REPLACE INTO ClazzReplicate(clazzPk, clazzDestination)
      SELECT DISTINCT Clazz.clazzUid AS clazzUid,
             :newNodeId AS clazzDestination
        FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               $JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1
                    ${Role.PERMISSION_CLAZZ_SELECT} 
                    $JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2
       WHERE UserSession.usClientNodeId = :newNodeId 
         AND Clazz.clazzLct != COALESCE(
             (SELECT clazzVersionId
                FROM ClazzReplicate
               WHERE clazzPk = Clazz.clazzUid
                 AND clazzDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(clazzPk, clazzDestination) DO UPDATE
             SET clazzPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([Clazz::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

     @Query("""
 REPLACE INTO ClazzReplicate(clazzPk, clazzDestination)
  SELECT DISTINCT Clazz.clazzUid AS clazzUid,
         UserSession.usClientNodeId AS clazzDestination
    FROM ChangeLog
         JOIN Clazz
             ON ChangeLog.chTableId = 6
                AND ChangeLog.chEntityPk = Clazz.clazzUid
         $JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1
                    ${Role.PERMISSION_CLAZZ_SELECT}
                    $JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND Clazz.clazzLct != COALESCE(
         (SELECT clazzVersionId
            FROM ClazzReplicate
           WHERE clazzPk = Clazz.clazzUid
             AND clazzDestination = UserSession.usClientNodeId), 0)
  /*psql ON CONFLICT(clazzPk, clazzDestination) DO UPDATE
      SET clazzPending = true
   */               
 """)
    @ReplicationRunOnChange([Clazz::class])
    @ReplicationCheckPendingNotificationsFor([Clazz::class])
    abstract suspend fun replicateOnChange()


    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): LiveData<Clazz?>

    @Query("SELECT * FROM Clazz WHERE clazzCode = :code")
    abstract suspend fun findByClazzCode(code: String): Clazz?

    @Query("SELECT * FROM Clazz WHERE clazzCode = :code")
    @RepoHttpAccessible
    @Repository(Repository.METHOD_DELEGATE_TO_WEB)
    abstract suspend fun findByClazzCodeFromWeb(code: String): Clazz?

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAllLive(): LiveData<List<Clazz>>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAll(): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Clazz?

    @Query("""
        SELECT Clazz.*, 
               HolidayCalendar.*, 
               School.*,
               CourseTerminology.*
          FROM Clazz 
               LEFT JOIN HolidayCalendar 
               ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid
               
               LEFT JOIN School 
               ON School.schoolUid = Clazz.clazzSchoolUid
               
               LEFT JOIN CourseTerminology
               ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid
         WHERE Clazz.clazzUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): ClazzWithHolidayCalendarAndSchoolAndTerminology?

    @Update
    abstract suspend fun updateAsync(entity: Clazz): Int


    @Query("SELECT * FROM Clazz WHERE clazzSchoolUid = :schoolUid " +
            "AND CAST(isClazzActive AS INTEGER) = 1 ")
    abstract suspend fun findAllClazzesBySchool(schoolUid: Long): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzSchoolUid = :schoolUid " +
            "AND CAST(isClazzActive AS INTEGER) = 1 ")
    abstract fun findAllClazzesBySchoolLive(schoolUid: Long)
            : DataSourceFactory<Int,Clazz>


    @Query("""
        SELECT Clazz.*, ClazzEnrolment.*,
               (SELECT COUNT(*) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = ${ROLE_STUDENT} 
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                       AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numStudents,
               (SELECT COUNT(*) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = ${ROLE_TEACHER}
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                        AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numTeachers,
               '' AS teacherNames,
               0 AS lastRecorded,
               CourseTerminology.*
          FROM PersonGroupMember
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    :permission
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}          
               LEFT JOIN ClazzEnrolment 
                    ON ClazzEnrolment.clazzEnrolmentUid =
                       COALESCE(
                       (SELECT ClazzEnrolment.clazzEnrolmentUid 
                          FROM ClazzEnrolment
                         WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid
                           AND ClazzEnrolment.clazzEnrolmentActive
                           AND ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid LIMIT 1), 0)
                LEFT JOIN CourseTerminology   
                ON CourseTerminology.ctUid = Clazz.clazzTerminologyUid           

         WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid
           AND PersonGroupMember.groupMemberActive 
           AND CAST(Clazz.isClazzActive AS INTEGER) = 1
           AND Clazz.clazzName like :searchQuery
           AND (Clazz.clazzUid NOT IN (:excludeSelectedClazzList))
           AND ( :excludeSchoolUid = 0 OR Clazz.clazzUid NOT IN (SELECT cl.clazzUid FROM Clazz AS cl WHERE cl.clazzSchoolUid = :excludeSchoolUid) ) 
           AND ( :excludeSchoolUid = 0 OR Clazz.clazzSchoolUid = 0 )
           AND ( :filter = 0 OR (CASE WHEN :filter = $FILTER_CURRENTLY_ENROLLED 
                                      THEN :currentTime BETWEEN Clazz.clazzStartTime AND Clazz.clazzEndTime
                                      ELSE :currentTime > Clazz.clazzEndTime 
                                      END))
           AND ( :selectedSchool = 0 OR Clazz.clazzSchoolUid = :selectedSchool)
      GROUP BY Clazz.clazzUid, ClazzEnrolment.clazzEnrolmentUid, CourseTerminology.ctUid
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
        excludeSchoolUid: Long, sortOrder: Int, filter: Int,
        currentTime: Long,
        permission: Long,
        selectedSchool: Long
    ) : DataSourceFactory<Int, ClazzWithListDisplayDetails>


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

    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("""
        SELECT EXISTS( 
               SELECT PrsGrpMbr.groupMemberPersonUid
                  FROM Clazz
                       ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                          :permission
                          ${Clazz.JOIN_FROM_SCOPEDGRANT_TO_PERSONGROUPMEMBER}
                 WHERE Clazz.clazzUid = :clazzUid
                   AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid)
    """)
    abstract suspend fun personHasPermissionWithClazz(accountPersonUid: Long, clazzUid: Long,
                                                      permission: Long) : Boolean

    @Query("""
        SELECT ScopedGrant.sgPermissions
          FROM Clazz
               JOIN ScopedGrant
                    ON ${Clazz.JOIN_SCOPEDGRANT_ON_CLAUSE}
               JOIN PersonGroupMember AS PrsGrpMbr
                    ON ScopedGrant.sgGroupUid = PrsGrpMbr.groupMemberGroupUid
         WHERE Clazz.clazzUid = :clazzUid
           AND (ScopedGrant.sgPermissions & ${Role.PERMISSION_PERSON_DELEGATE}) > 0
           AND PrsGrpMbr.groupMemberPersonUid = :accountPersonUid
    """)
    abstract suspend fun selectDelegatablePermissions(
        accountPersonUid: Long,
        clazzUid: Long
    ): List<Long>

    @Query("""
        SELECT Clazz.*, 
               HolidayCalendar.*, 
               School.*,
               (SELECT COUNT(*) 
                  FROM ClazzEnrolment 
                 WHERE ClazzEnrolment.clazzEnrolmentClazzUid = Clazz.clazzUid 
                   AND clazzEnrolmentRole = $ROLE_STUDENT 
                   AND :currentTime BETWEEN ClazzEnrolment.clazzEnrolmentDateJoined 
                        AND ClazzEnrolment.clazzEnrolmentDateLeft) AS numStudents,
               (SELECT COUNT(*) 
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
        WHERE Clazz.clazzUid = :clazzUid""")
    abstract fun getClazzWithDisplayDetails(clazzUid: Long, currentTime: Long): LiveData<ClazzWithDisplayDetails?>


    /**
     * Used for scheduling purposes - get a list of classes with the applicable holiday calendar.
     * This might be the holiday calendar specifeid by the class (if any) or the the calendar
     * specified for the associated school.
     */
    @Query("""
        SELECT Clazz.*, 
               HolidayCalendar.*, 
               School.*,
               CourseTerminology.*
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
                
        WHERE :filterUid = 0 
           OR Clazz.clazzUid = :filterUid
    """)
    abstract fun findClazzesWithEffectiveHolidayCalendarAndFilter(filterUid: Long): List<ClazzWithHolidayCalendarAndSchoolAndTerminology>

    @Query("SELECT Clazz.*, School.* FROM Clazz LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid WHERE clazz.clazzUid = :clazzUid")
    abstract suspend fun getClazzWithSchool(clazzUid: Long): ClazzWithSchool?

}
