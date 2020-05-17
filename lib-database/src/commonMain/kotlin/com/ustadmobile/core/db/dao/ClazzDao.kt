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
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao(selectPermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
        updatePermissionCondition = ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE +
        ENTITY_LEVEL_PERMISSION_CONDITION2,
        insertPermissionCondition = TABLE_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_INSERT +
        TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzDao : BaseDao<Clazz>, OneToManyJoinDao<Clazz> {

    @QueryLiveTables(["Clazz", "ClazzMember"])
    @Query("SELECT " +
            " (SELECT COUNT(*) FROM Clazz Where CAST(Clazz.isClazzActive AS INTEGER) = 1) as numClazzes, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT + ") as numStudents, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_TEACHER + ") as numTeachers, " +
            " ((SELECT SUM(Clazz.attendanceAverage) FROM Clazz WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 ) / " +
            " (SELECT COUNT(*) FROM Clazz Where CAST(Clazz.isClazzActive AS INTEGER) = 1)) as attendanceAverage ")
    abstract fun getClazzSummaryLiveData(): DoorLiveData<ClazzAverage?>


    @Insert
    abstract override fun insert(entity: Clazz): Long

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz?

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Clazz?>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAllAsList(): List<Clazz>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract fun findAllLive(): DoorLiveData<List<Clazz>>

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Clazz?

    @Query("""SELECT Clazz.*, HolidayCalendar.* FROM Clazz 
            LEFT JOIN HolidayCalendar ON Clazz.clazzHolidayUMCalendarUid = HolidayCalendar.umCalendarUid
            WHERE Clazz.clazzUid = :uid""")
    abstract suspend fun findByUidWithHolidayCalendarAsync(uid: Long): ClazzWithHolidayCalendar?

    @Update
    abstract suspend fun updateAsync(entity: Clazz): Int

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long


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


    fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        val auditLog = AuditLog(fromPersonUid, Clazz.TABLE_ID, toPersonUid)
        insertAuditLog(auditLog)
    }

    fun insertClazz(entity: Clazz, loggedInPersonUid: Long) {
        val personUid = insert(entity)
        createAuditLog(personUid, loggedInPersonUid)
    }

    suspend fun insertClazzAsync(entity: Clazz, loggedInPersonUid: Long): Long {
        val result = insertAsync(entity)
        createAuditLog(entity.clazzUid, loggedInPersonUid)
        return result
    }

    fun updateClazz(entity: Clazz, loggedInPersonUid: Long) {
        update(entity)
        createAuditLog(entity.clazzUid, loggedInPersonUid)
    }

    suspend fun updateClazzAsync(entity: Clazz, loggedInPersonUid: Long): Int {
        //long personUid = insert(entity);
        val result = updateAsync(entity)
        createAuditLog(entity.clazzUid, loggedInPersonUid)
        return result
    }

    @Query("UPDATE Clazz SET isClazzActive = 0 WHERE clazzUid = :clazzUid")
    abstract suspend fun inactivateClazz(clazzUid: Long) : Int

    @Query(CLAZZ_SELECT + CLAZZ_WHERE_CLAZZMEMBER )
    abstract fun findAllClazzesByPersonUid(personUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT +
            " FROM Clazz " +
            " WHERE " + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
            ENTITY_LEVEL_PERMISSION_CONDITION2)
    abstract fun findAllClazzesByPermission(accountPersonUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_SELECT FROM Clazz WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 ")
    abstract fun findAllClazzes(): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_SELECT FROM Clazz WHERE clazzLocationUid in (:locations)")
    abstract fun findAllClazzesInLocationList(locations: List<Long>): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_SELECT FROM Clazz WHERE clazzLocationUid in (:locations)")
    abstract suspend fun findAllClazzesInLocationListAsync(locations: List<Long>) :
            List<ClazzWithNumStudents>

    @Query("SELECT * FROM Clazz WHERE clazzUid in (:clazzUidList) AND CAST(isClazzActive AS INTEGER) = 1")
    abstract suspend fun findClazzesByUidListAsync(clazzUidList: List<Long>): List<Clazz>

    @Query(SELECT_ACTIVE_CLAZZES)
    abstract suspend fun findAllActiveClazzesAsync() : List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzLocationUid IN (:allLocations) "
            + " OR clazzUid in (:allClasses) AND CAST(isClazzActive AS INTEGER) = 1")
    abstract suspend fun findAllClazzesInUidAndLocationAsync(allLocations: List<Long>,
                                                     allClasses: List<Long>) : List<Clazz>

    @Query("SELECT * FROM Clazz WHERE  clazzUid in (:allClasses) AND isClazzActive = 1")
    abstract suspend fun findAllClazzesInUidAsync(allClasses: List<Long>) : List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzLocationUid IN (:allLocations) " +
            " AND CAST(isClazzActive AS INTEGER) = 1")
    abstract suspend fun findAllClazzesInLocationAsync(allLocations: List<Long>) : List<Clazz>

    suspend fun findAllClazzesByLocationAndUidList(allLocations: List<Long>,
                                           allClazzes: List<Long>): List<Clazz> {
        if (allLocations.isEmpty() && allClazzes.isEmpty()) {
            return findAllActiveClazzesAsync()
        } else {
            if (allLocations.isEmpty()) {
                return findAllClazzesInUidAsync(allClazzes)
            } else if (allClazzes.isEmpty()) {
                return findAllClazzesInLocationAsync(allLocations)
            } else {
                return findAllClazzesInUidAndLocationAsync(allLocations, allClazzes)
            }
        }
    }

    @Query("$CLAZZ_SELECT FROM Clazz WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 ")
    abstract fun findAllActiveClazzes(): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT + CLAZZ_WHERE_CLAZZMEMBER +
            " WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " AND ( :schoolUid = 0 OR Clazz.clazzUid NOT IN (SELECT cl.clazzUid FROM Clazz AS cl WHERE cl.clazzSchoolUid = :schoolUid) ) " +
            " AND ( :schoolUid = 0 OR Clazz.clazzSchoolUid = 0 )" +
            " ORDER BY Clazz.clazzName ASC")
    abstract fun findAllActiveClazzesSortByNameAsc(
            searchQuery: String, personUid: Long, schoolUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT +  CLAZZ_WHERE_CLAZZMEMBER +
            " WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " AND ( :schoolUid = 0 OR Clazz.clazzUid NOT IN (SELECT cl.clazzUid FROM Clazz AS cl WHERE cl.clazzSchoolUid = :schoolUid) ) " +
            " AND ( :schoolUid = 0 OR Clazz.clazzSchoolUid = 0 )" +
            " ORDER BY Clazz.clazzName DESC")
    abstract fun findAllActiveClazzesSortByNameDesc(
            searchQuery: String, personUid: Long, schoolUid: Long
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT + CLAZZ_WHERE_CLAZZMEMBER +
            " AND CAST(Clazz.isClazzActive AS INTEGER) = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.attendanceAverage ASC ")
    abstract fun findAllActiveClazzesSortByAttendanceAsc(
            searchQuery: String, personUid: Long
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT +  CLAZZ_WHERE_CLAZZMEMBER +
            " AND CAST(Clazz.isClazzActive AS INTEGER) = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.attendanceAverage DESC ")
    abstract fun findAllActiveClazzesSortByAttendanceDesc(
            searchQuery: String, personUid: Long
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_SELECT + CLAZZ_WHERE_CLAZZMEMBER +
            " AND CAST(Clazz.isClazzActive AS INTEGER) = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY teacherNames ASC ")
    abstract fun findAllActiveClazzesSortByTeacherAsc(
            searchQuery: String, personUid: Long
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and CAST(isClazzActive AS INTEGER) = 1")
    abstract suspend fun findByClazzNameAsync(name: String): List<Clazz>

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and CAST(isClazzActive AS INTEGER) = 1")
    abstract fun findByClazzName(name: String): List<Clazz>

    @Query("SELECT Clazz.*, (:personUid) AS personUid, " +
            "(SELECT COUNT(*) FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT + " ) " +
            " AS numStudents, " +
            "(SELECT (EXISTS (SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
            " AND clazzMemberClazzUid = Clazz.clazzUid  AND CAST(clazzMemberActive AS INTEGER) = 1 " +
            " ))) AS enrolled " +
            "FROM Clazz WHERE CAST(Clazz.isClazzActive AS INTEGER) = 1 ORDER BY Clazz.clazzName ASC")
    abstract fun findAllClazzesWithEnrollmentByPersonUid(personUid: Long): DataSource.Factory<Int, ClazzWithEnrollment>

    @Query(CLAZZ_SELECT +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    abstract fun findAllClazzesByPersonUidAsList(personUid: Long): List<ClazzWithNumStudents>

    @Query("Update Clazz SET attendanceAverage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            " LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = 1) * 1.0 " +
            " /  " +
            "MAX(1, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            "LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            ")) * 1.0 " +
            "Where Clazz.clazzUid = :clazzUid")
    abstract fun updateAttendancePercentage(clazzUid: Long)

    @Query("SELECT " +
            "   (" +
            "       SELECT COUNT(*) " +
            "       FROM ClazzLogAttendanceRecord  " +
            "       LEFT JOIN ClazzLog " +
            "           ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "       WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            "           AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            "           AND ClazzLogAttendanceRecord.attendanceStatus =  " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            "           AND ClazzLog.clazzLogUid " +
            "               NOT IN " +
            "               (SELECT clazzLogUid FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid " +
            "           ORDER BY logDate DESC LIMIT 1)" +
            "   ) * 1.0" +
            "   /  " +
            "   MAX(1, " +
            "       (SELECT COUNT(*) " +
            "           FROM ClazzLogAttendanceRecord  " +
            "            LEFT JOIN ClazzLog " +
            "             ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "            WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            "             AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            "             AND ClazzLog.clazzLogUid " +
            "               NOT IN " +
            "               (SELECT clazzLogUid FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid " +
            "               ORDER BY logDate DESC LIMIT 1)" +
            "       )" +
            "   ) * 1.0 " +
            "   AS percentage")
    abstract fun findClazzAttendancePercentageWithoutLatestClazzLog(clazzUid: Long): Float

    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("SELECT EXISTS (SELECT 1 FROM Clazz WHERE Clazz.clazzUid = :clazzUid AND (" +
            ENTITY_LEVEL_PERMISSION_CONDITION1 +
            " :permission" + ENTITY_LEVEL_PERMISSION_CONDITION2 + "))")
    abstract suspend fun personHasPermissionWithClazz(accountPersonUid: Long, clazzUid: Long,
                                                      permission: Long) : Boolean

    @QueryLiveTables(["Person", "PersonGroupMember"])
    @Query("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    abstract fun personHasPermissionLive(accountPersonUid: Long, permission: Long)
            : DoorLiveData<Boolean>

    @Query("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    abstract suspend fun personHasPermission(accountPersonUid: Long, permission: Long): Boolean

    @Query("SELECT Clazz.*," +
            "Location.timeZone " +
            " FROM Clazz " +
            "LEFT JOIN Location ON Clazz.clazzLocationUid = Location.locationUid " +
            "WHERE " + ENTITY_LEVEL_PERMISSION_CONDITION1
            + Role.PERMISSION_CLAZZ_SELECT + ENTITY_LEVEL_PERMISSION_CONDITION2)
    abstract fun findAllClazzesWithSelectPermission(accountPersonUid: Long): List<ClazzWithTimeZone>

    @Query("SELECT clazzName FROM Clazz WHERE clazzUid = :clazzUid")
    abstract fun getClazzName(clazzUid: Long): String?

    @Query("SELECT clazzName FROM Clazz WHERE clazzUid = :clazzUid")
    abstract suspend fun getClazzNameAsync(clazzUid: Long): String?

    @Query("SELECT Person.* " +
            "FROM PersonGroupMember " +
            "JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
            "JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
            "LEFT JOIN Person ON PersonGroupMember.groupMemberPersonUid = Person.personUid " +
            "WHERE (" +
            "(EntityRole.ertableId = " + Clazz.TABLE_ID +
            " AND EntityRole.erEntityUid = :clazzUid) " +
            "OR" +
            "(EntityRole.ertableId = " + Location.TABLE_ID +
            " AND EntityRole.erEntityUid IN (SELECT locationAncestorAncestorLocationUid " +
            " FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid = " +
            " (SELECT clazzLocationUid FROM Clazz WHERE clazzUid = :clazzUid)))) " +
            "AND Role.roleUid = :roleUid AND Person.personUid != null")
    abstract fun findPeopleWithRoleAssignedToClazz(clazzUid: Long, roleUid: Long): List<Person>


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
     */
    @Query("""
        SELECT Clazz.*, HolidayCalendar.*
        FROM Clazz 
        LEFT JOIN HolidayCalendar ON ((clazz.clazzHolidayUMCalendarUid != 0 AND HolidayCalendar.umCalendarUid = clazz.clazzHolidayUMCalendarUid)
         OR clazz.clazzHolidayUMCalendarUid = 0 AND clazz.clazzSchoolUid = 0 AND HolidayCalendar.umCalendarUid = 
            (SELECT schoolHolidayCalendarUid FROM School WHERE schoolUid = clazz.clazzSchoolUid))
        WHERE :filterUid = 0 OR Clazz.clazzUid = :filterUid
    """)
    abstract fun findClazzesWithHolidayCalendarAndFilter(filterUid: Long): List<ClazzWithHolidayCalendar>

    companion object {

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
                "OR" +
                "(EntityRole.ertableId = " + Location.TABLE_ID +
                " AND EntityRole.erEntityUid IN (SELECT locationAncestorAncestorLocationUid " +
                " FROM LocationAncestorJoin WHERE locationAncestorChildLocationUid = " +
                " Clazz.clazzLocationUid))" +
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
