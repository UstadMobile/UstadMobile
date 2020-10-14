package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLog.Companion.STATUS_RECORDED
import com.ustadmobile.lib.db.entities.ClazzMember.Companion.ROLE_STUDENT
import com.ustadmobile.lib.db.entities.ClazzMember.Companion.ROLE_TEACHER

@UmRepository
@Dao
abstract class ClazzDao : BaseDao<Clazz>, OneToManyJoinDao<Clazz> {

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Clazz?>

    @Query("SELECT * FROM Clazz WHERE clazzCode = :code")
    abstract suspend fun findByClazzCode(code: String): Clazz?

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAllLive(): DoorLiveData<List<Clazz>>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAll(): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Clazz?

    @Query("""SELECT Clazz.*, HolidayCalendar.*, School.* FROM Clazz 
            LEFT JOIN HolidayCalendar ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid
            LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid
            WHERE Clazz.clazzUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): ClazzWithHolidayCalendarAndSchool?

    @Update
    abstract suspend fun updateAsync(entity: Clazz): Int


    @Query("SELECT * FROM Clazz WHERE clazzSchoolUid = :schoolUid " +
            "AND CAST(isClazzActive AS INTEGER) = 1 ")
    abstract suspend fun findAllClazzesBySchool(schoolUid: Long): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzSchoolUid = :schoolUid " +
            "AND CAST(isClazzActive AS INTEGER) = 1 ")
    abstract fun findAllClazzesBySchoolLive(schoolUid: Long)
            : DataSource.Factory<Int,Clazz>


    @Query("UPDATE Clazz SET clazzSchoolUid = :schoolUid, " +
            " clazzLastChangedBy = (SELECT nodeClientId FROM SyncNode) WHERE clazzUid = :clazzUid ")
    abstract suspend fun updateSchoolOnClazzUid(clazzUid: Long, schoolUid: Long)

    /**
     * Does not deactivate the clazz, dissassociates a school from the class.
     */
    override suspend fun deactivateByUids(uidList: List<Long>) = assignClassesToSchool(uidList, 0L)

    suspend fun assignClassesToSchool(uidList: List<Long>, schoolUid: Long) {
        uidList.forEach {
            updateSchoolOnClazzUid(it, schoolUid)
        }
    }

    @Query("""
        SELECT Clazz.*, ClazzMember.*,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = ${ClazzMember.ROLE_STUDENT}) AS numStudents,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = ${ClazzMember.ROLE_TEACHER}) AS numTeachers,
        '' AS teacherNames,
        0 AS lastRecorded
        FROM 
        Clazz
        LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid =
            COALESCE((SELECT ClazzMember.clazzMemberUid FROM ClazzMember
             WHERE
             ClazzMember.clazzMemberPersonUid = :personUid
             AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid LIMIT 1), 0)
        WHERE
        CAST(Clazz.isClazzActive AS INTEGER) = 1
        AND Clazz.clazzName like :searchQuery
        AND ( :excludeSchoolUid = 0 OR Clazz.clazzUid NOT IN (SELECT cl.clazzUid FROM Clazz AS cl WHERE cl.clazzSchoolUid = :excludeSchoolUid) ) 
        AND ( :excludeSchoolUid = 0 OR Clazz.clazzSchoolUid = 0 )
        AND :personUid IN (
        $ENTITY_PERSONS_WITH_PERMISSION
        )
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
    abstract fun findClazzesWithPermission(searchQuery: String, personUid: Long,
                           excludeSchoolUid: Long, sortOrder: Int, permission: Long)
            : DataSource.Factory<Int, ClazzWithListDisplayDetails>

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and CAST(isClazzActive AS INTEGER) = 1")
    abstract fun findByClazzName(name: String): List<Clazz>

    @Query("""
        UPDATE Clazz SET attendanceAverage = 
        CAST((SELECT SUM(clazzLogNumPresent) FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid AND clazzLogStatusFlag = 4) AS REAL) /
        CAST(MAX(1.0, (SELECT SUM(clazzLogNumPresent) + SUM(clazzLogNumPartial) + SUM(clazzLogNumAbsent)
        FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid AND clazzLogStatusFlag = $STATUS_RECORDED)) AS REAL),
        clazzLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE clazzUid = :clazzUid
    """)
    abstract fun updateClazzAttendanceAverage(clazzUid: Long)

    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("SELECT EXISTS(SELECT 1 FROM Clazz WHERE " +
            "Clazz.clazzUid = :clazzUid AND :accountPersonUid IN ($ENTITY_PERSONS_WITH_PERMISSION))")
    abstract suspend fun personHasPermissionWithClazz(accountPersonUid: Long, clazzUid: Long,
                                                      permission: Long) : Boolean

    @Query("""SELECT Clazz.*, HolidayCalendar.*, School.*,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = $ROLE_STUDENT) AS numStudents,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = $ROLE_TEACHER) AS numTeachers
        FROM Clazz 
        LEFT JOIN HolidayCalendar ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid
        LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid
        WHERE Clazz.clazzUid = :clazzUid""")
    abstract fun getClazzWithDisplayDetails(clazzUid: Long): DoorLiveData<ClazzWithDisplayDetails?>


    /**
     * Used for scheduling purposes - get a list of classes with the applicable holiday calendar.
     * This might be the holiday calendar specifeid by the class (if any) or the the calendar
     * specified for the associated school.
     */
    @Query("""
        SELECT Clazz.*, HolidayCalendar.*, School.*
        FROM Clazz 
        LEFT JOIN HolidayCalendar ON ((clazz.clazzHolidayUMCalendarUid != 0 AND HolidayCalendar.umCalendarUid = clazz.clazzHolidayUMCalendarUid)
         OR clazz.clazzHolidayUMCalendarUid = 0 AND clazz.clazzSchoolUid = 0 AND HolidayCalendar.umCalendarUid = 
            (SELECT schoolHolidayCalendarUid FROM School WHERE schoolUid = clazz.clazzSchoolUid))
        LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid
        WHERE :filterUid = 0 OR Clazz.clazzUid = :filterUid
    """)
    abstract fun findClazzesWithEffectiveHolidayCalendarAndFilter(filterUid: Long): List<ClazzWithHolidayCalendarAndSchool>

    @Query("SELECT Clazz.*, School.* FROM Clazz LEFT JOIN School ON School.schoolUid = Clazz.clazzSchoolUid WHERE clazz.clazzUid = :clazzUid")
    abstract suspend fun getClazzWithSchool(clazzUid: Long): ClazzWithSchool?

    companion object {

        const val SORT_CLAZZNAME_ASC = 1

        const val SORT_CLAZZNAME_DESC = 2

        const val SORT_ATTENDANCE_ASC = 3

        const val SORT_ATTENDANCE_DESC = 4

        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person.PersonUid FROM Person
            LEFT JOIN PersonGroupMember ON Person.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (
            ((EntityRole.ertableId = ${Clazz.TABLE_ID} AND EntityRole.erEntityUid = Clazz.clazzUid) OR
            (EntityRole.ertableId = ${School.TABLE_ID} AND EntityRole.erEntityUid = Clazz.clazzSchoolUid)
            )
            AND
            (Role.rolePermissions &  
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

        const val ENTITY_PERSONS_WITH_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 :permission $ENTITY_PERSONS_WITH_PERMISSION_PT2"

        const val ENTITY_PERSON_WITH_SELECT_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 " +
                "${Role.PERMISSION_CLAZZ_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT2"

        private const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"
    }


}
