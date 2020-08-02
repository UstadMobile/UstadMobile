package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION1
import com.ustadmobile.core.db.dao.ClazzDao.Companion.TABLE_LEVEL_PERMISSION_CONDITION2
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ClazzLog.Companion.STATUS_RECORDED

@UmDao(selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
        updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
        insertPermissionCondition = TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT +
        TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzDao : BaseDao<Clazz>, OneToManyJoinDao<Clazz> {

    @Insert
    abstract override fun insert(entity: Clazz): Long

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Clazz?>

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
        SELECT Clazz.*,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = 1) AS numStudents,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = 2) AS numTeachers,
        '' AS teacherNames,
        0 AS lastRecorded
        FROM 
        Clazz
        WHERE
        CAST(Clazz.isClazzActive AS INTEGER) = 1
        AND Clazz.clazzName like :searchQuery
        AND ( :excludeSchoolUid = 0 OR Clazz.clazzUid NOT IN (SELECT cl.clazzUid FROM Clazz AS cl WHERE cl.clazzSchoolUid = :excludeSchoolUid) ) 
        AND ( :excludeSchoolUid = 0 OR Clazz.clazzSchoolUid = 0 )
        AND :personUid IN (
        $ENTITY_PERSON_WITH_SELECT_PERMISSION
        )
        ORDER BY CASE :sortOrder
            WHEN $SORT_CLAZZNAME_ASC THEN Clazz.clazzName
            WHEN $SORT_ATTENDANCE_ASC THEN Clazz.attendanceAverage
            ELSE 0
        END ASC,
        CASE :sortOrder
            WHEN $SORT_CLAZZNAME_DESC THEN Clazz.clazzName
            WHEN $SORT_ATTENDANCE_DESC THEN Clazz.attendanceAverage
            ELSE 0
        END DESC
    """)
    abstract fun findClazzesWithPermission(searchQuery: String, personUid: Long, excludeSchoolUid: Long, sortOrder: Int): DataSource.Factory<Int, ClazzWithNumStudents>

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

    @Query("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    abstract suspend fun personHasPermission(accountPersonUid: Long, permission: Long): Boolean



    @Query("""SELECT Clazz.*, HolidayCalendar.*, School.*,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = 1) AS numStudents,
        (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND clazzMemberRole = 2) AS numTeachers
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
            (EntityRole.ertableId = ${Clazz.TABLE_ID} AND 
            EntityRole.erEntityUid = Clazz.clazzUid AND
            (Role.rolePermissions &  
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

        const val ENTITY_PERSONS_WITH_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 :permission $ENTITY_PERSONS_WITH_PERMISSION_PT2"

        const val ENTITY_PERSON_WITH_SELECT_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 " +
                "${Role.PERMISSION_CLAZZ_SELECT} $ENTITY_PERSONS_WITH_PERMISSION_PT2"


        const val ENTITY_LEVEL_PERMISSION_CONDITION1 =
                " CASE WHEN EXISTS (SELECT admin FROM Person WHERE personUid " +
                "= :accountPersonUid) THEN (SELECT admin FROM Person WHERE personUid = :accountPersonUid) ELSE 0 END " +
                " OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                "WHERE PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND (" +
                "(EntityRole.ertableId = " + Clazz.TABLE_ID +
                " AND EntityRole.erEntityUid = Clazz.clazzUid) " +

                ") AND (Role.rolePermissions & "

        const val ENTITY_LEVEL_PERMISSION_CONDITION2 = ") > 0)"

        const val TABLE_LEVEL_PERMISSION_CONDITION1 = " CASE WHEN EXISTS (SELECT admin FROM Person WHERE personUid " +
                "= :accountPersonUid) THEN (SELECT admin FROM Person WHERE personUid = :accountPersonUid) ELSE 0 END " +
                " OR " +
                " EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                " WHERE " +
                " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND EntityRole.erTableId = " + Clazz.TABLE_ID +
                " AND Role.rolePermissions & "

        const val TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)"

        private const val CLAZZ_SELECT = " SELECT Clazz.*, " +
                "(SELECT ClazzLog.logDate FROM ClazzLog " +
                "WHERE ClazzLog.clazzLogClazzUid = Clazz.clazzUid AND CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
                "ORDER BY ClazzLog.logDate DESC LIMIT 1) AS lastRecorded, " +
                "(SELECT COUNT(*) " +
                "   FROM ClazzMember " +
                "   LEFT JOIN Person AS SP ON SP.personUid = ClazzMember.clazzMemberPersonUid " +
                "   WHERE " +
                "   ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "   AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT +
                "   AND CAST(ClazzMember.clazzMemberActive AS INTEGER)  = 1 " +
                "   AND CAST(SP.active AS INTEGER) = 1 ) AS numStudents, " +
                " (SELECT COUNT(*) FROM ClazzMember " +
                "   LEFT JOIN Person AS CP ON CP.personUid = ClazzMember.clazzMemberPersonUid " +
                "   WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "   AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_TEACHER +
                "   AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 AND CAST(CP.active AS INTEGER) = 1 ) AS numTeachers, " +
                " (SELECT GROUP_CONCAT(Person.firstNames || ' ' ||  Person.lastName ) as teacherName " +
                "   FROM Person where Person.personUid in (SELECT ClazzMember.clazzMemberPersonUid " +
                "   FROM ClazzMember WHERE ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_TEACHER +
                "   AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid" +
                "   AND CAST(ClazzMember.clazzMemberActive AS INTEGER)  = 1) " +
                " ) AS teacherNames "

        private const val CLAZZ_WHERE_CLAZZMEMBER =
                " FROM Clazz " +
                " LEFT JOIN Person ON Person.personUid = :personUid "

        private const val SELECT_CLAZZ_WHERE_PERMISSION = " SELECT " +
                "   Clazz.*, " +
                "   (SELECT COUNT(*) " +
                "       FROM ClazzMember WHERE " +
                "       ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "       AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT +
                "       AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1" +
                "       ) AS numStudents, " +
                "   (SELECT COUNT(*) FROM ClazzMember " +
                "       WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "       AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_TEACHER +
                "       AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
                "   ) AS numTeachers, " +
                "   (SELECT GROUP_CONCAT" +
                "       (Person.firstNames || ' ' ||  Person.lastName ) as teacherName " +
                "       FROM Person where Person.personUid in " +
                "       (SELECT ClazzMember.clazzMemberPersonUid " +
                "           FROM ClazzMember WHERE ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_TEACHER +
                "           AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid" +
                "           AND CAST(ClazzMember.clazzMemberActive AS INTEGER)  = 1" +
                "       ) " +
                "   ) AS teacherNames "

        private const val SELECT_ACTIVE_CLAZZES = "SELECT * FROM Clazz WHERE CAST(isClazzActive AS INTEGER) = 1"
    }


}
