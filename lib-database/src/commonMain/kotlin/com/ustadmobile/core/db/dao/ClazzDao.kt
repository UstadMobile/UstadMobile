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
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
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
abstract class ClazzDao : BaseDao<Clazz> {

    @get:Query("SELECT " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1) as numClazzes, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT + ") as numStudents, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER + ") as numTeachers, " +
            " ((SELECT SUM(Clazz.attendanceAverage) FROM Clazz WHERE Clazz.clazzActive = 1 ) / " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1)) as attendanceAverage ")
    abstract val clazzSummaryLiveData: DoorLiveData<ClazzAverage>


    @Insert
    abstract override fun insert(entity: Clazz): Long

    @Insert
    abstract fun insertAsync(entity: Clazz, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUid(uid: Long): Clazz

    @Query("SELECT * From Clazz WHERE clazzUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<Clazz>

    @Query("SELECT * FROM Clazz")
    abstract fun findAllAsList(): List<Clazz>

    @Query("SELECT * FROM Clazz")
    abstract fun findAllLive(): DoorLiveData<List<Clazz>>

    @Query("SELECT * FROM Clazz WHERE clazzUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<Clazz>)

    @Update
    abstract fun updateAsync(entity: Clazz, resultObject: UmCallback<Int>)

    @Insert
    abstract fun insertAuditLog(entity: AuditLog): Long

    fun createAuditLog(toPersonUid: Long, fromPersonUid: Long) {
        val auditLog = AuditLog(fromPersonUid, Clazz.TABLE_ID, toPersonUid)
        insertAuditLog(auditLog)
    }

    fun insertClazz(entity: Clazz, loggedInPersonUid: Long) {
        val personUid = insert(entity)
        createAuditLog(personUid, loggedInPersonUid)
    }

    fun insertClazzAsync(entity: Clazz, loggedInPersonUid: Long, callback: UmCallback<Long>) {
        //long personUid = insert(entity);
        insertAsync(entity, object : UmCallback<Long> {
            override fun onSuccess(result: Long?) {
                createAuditLog(entity.clazzUid, loggedInPersonUid)
                callback.onSuccess(result)
            }

            override fun onFailure(exception: Throwable?) {
                callback.onFailure(exception)
            }
        })

    }

    fun updateClazz(entity: Clazz, loggedInPersonUid: Long) {
        update(entity)
        createAuditLog(entity.clazzUid, loggedInPersonUid)
    }

    fun updateClazzAsync(entity: Clazz, loggedInPersonUid: Long, callback: UmCallback<Int>) {
        //long personUid = insert(entity);
        updateAsync(entity, object : UmCallback<Int> {
            override fun onSuccess(result: Int?) {
                createAuditLog(entity.clazzUid, loggedInPersonUid)
                callback.onSuccess(result)
            }

            override fun onFailure(exception: Throwable?) {
                callback.onFailure(exception)
            }
        })

    }

    @Query("UPDATE Clazz SET clazzActive = 0 WHERE clazzUid = :clazzUid")
    abstract fun inactivateClazz(clazzUid: Long, resultCallback: UmCallback<Int>)

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
            "  WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.clazzMemberActive = 1)")
    abstract fun findAllClazzesByPersonUid(personUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz " +
            " WHERE " + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_SELECT +
            ENTITY_LEVEL_PERMISSION_CONDITION2)
    abstract fun findAllClazzesByPermission(accountPersonUid: Long): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_WHERE FROM Clazz ")
    abstract fun findAllClazzes(): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_WHERE FROM Clazz WHERE clazzLocationUid in (:locations)")
    abstract fun findAllClazzesInLocationList(locations: List<Long>): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("$CLAZZ_WHERE FROM Clazz WHERE clazzLocationUid in (:locations)")
    abstract fun findAllClazzesInLocationListAsync(locations: List<Long>,
                                                   resultList: UmCallback<List<ClazzWithNumStudents>>)


    @Query("SELECT * FROM Clazz WHERE clazzUid in (:clazzUidList) AND clazzActive = 1")
    abstract fun findClazzesByUidListAsync(clazzUidList: List<Long>,
                                           resultList: UmCallback<List<Clazz>>)

    @Query("SELECT * FROM Clazz WHERE clazzActive = 1")
    abstract fun findAllActiveClazzesAsync(resultList: UmCallback<List<Clazz>>)

    @Query("SELECT * FROM Clazz WHERE clazzLocationUid IN (:allLocations) " + " OR clazzUid in (:allClasses) AND clazzActive = 1")
    abstract fun findAllClazzesInUidAndLocationAsync(allLocations: List<Long>,
                                                     allClasses: List<Long>,
                                                     resultList: UmCallback<List<Clazz>>)

    @Query("SELECT * FROM Clazz WHERE  clazzUid in (:allClasses) AND clazzActive = 1")
    abstract fun findAllClazzesInUidAsync(allClasses: List<Long>,
                                          resultList: UmCallback<List<Clazz>>)

    @Query("SELECT * FROM Clazz WHERE clazzLocationUid IN (:allLocations) " + " AND clazzActive = 1")
    abstract fun findAllClazzesInLocationAsync(allLocations: List<Long>,
                                               resultList: UmCallback<List<Clazz>>)

    fun findAllClazzesByLocationAndUidList(allLocations: List<Long>,
                                           allClazzes: List<Long>,
                                           resultList: UmCallback<List<Clazz>>) {
        if (allLocations.isEmpty() && allClazzes.isEmpty()) {
            findAllActiveClazzesAsync(resultList)
        } else {
            if (allLocations.isEmpty()) {
                findAllClazzesInUidAsync(allClazzes, resultList)
            } else if (allClazzes.isEmpty()) {
                findAllClazzesInLocationAsync(allLocations, resultList)
            } else {
                findAllClazzesInUidAndLocationAsync(allLocations, allClazzes, resultList)
            }
        }
    }

    @Query("$CLAZZ_WHERE FROM Clazz WHERE Clazz.clazzActive = 1 ")
    abstract fun findAllActiveClazzes(): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.clazzName ASC")
    abstract fun findAllActiveClazzesSortByNameAsc(
            searchQuery: String): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.clazzName DESC")
    abstract fun findAllActiveClazzesSortByNameDesc(
            searchQuery: String
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.attendanceAverage ASC ")
    abstract fun findAllActiveClazzesSortByAttendanceAsc(
            searchQuery: String
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY Clazz.attendanceAverage DESC ")
    abstract fun findAllActiveClazzesSortByAttendanceDesc(
            searchQuery: String
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE Clazz.clazzActive = 1 " +
            " AND Clazz.clazzName like :searchQuery" +
            " ORDER BY teacherNames ASC ")
    abstract fun findAllActiveClazzesSortByTeacherAsc(
            searchQuery: String
    ): DataSource.Factory<Int, ClazzWithNumStudents>

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and clazzActive = 1")
    abstract fun findByClazzNameAsync(name: String, resultList: UmCallback<List<Clazz>>)

    @Query("SELECT * FROM Clazz WHERE clazzName = :name and clazzActive = 1")
    abstract fun findByClazzName(name: String): List<Clazz>


    @Query("SELECT " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1) as numClazzes, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT + ") as numStudents, " +
            " (SELECT COUNT(*) FROM ClazzMember WHERE ClazzMember.clazzMemberActive = 1 " +
            " AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER + ") as numTeachers, " +
            " ((SELECT SUM(Clazz.attendanceAverage) FROM Clazz WHERE Clazz.clazzActive = 1 ) / " +
            " (SELECT COUNT(*) FROM Clazz Where Clazz.clazzActive = 1)) as attendanceAverage ")
    abstract fun getClazzSummaryAsync(resultObject: UmCallback<ClazzAverage>)

    @Query("SELECT Clazz.*, (:personUid) AS personUid, " +
            "(SELECT COUNT(*) FROM ClazzMember " +
            "WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid AND ClazzMember.role = 1) " +
            " AS numStudents, " +
            "(SELECT (EXISTS (SELECT * FROM ClazzMember WHERE clazzMemberPersonUid = :personUid " +
            " AND clazzMemberClazzUid = Clazz.clazzUid  AND clazzMemberActive = 1 " +
            " ))) AS enrolled " +
            "FROM Clazz WHERE Clazz.clazzActive = 1 ORDER BY Clazz.clazzName ASC")
    abstract fun findAllClazzesWithEnrollmentByPersonUid(personUid: Long): DataSource.Factory<Int, ClazzWithEnrollment>

    @Query(CLAZZ_WHERE +
            " FROM Clazz WHERE :personUid in " +
            " (SELECT ClazzMember.clazzMemberPersonUid FROM ClazzMember " +
            " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid)")
    abstract fun findAllClazzesByPersonUidAsList(personUid: Long): List<ClazzWithNumStudents>

    @Query("Update Clazz SET attendanceAverage " +
            " = (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            " LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            " WHERE ClazzLog.done = 1 " +
            " AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            " AND ClazzLogAttendanceRecord.attendanceStatus = 1) * 1.0 " +
            " /  " +
            "MAX(1, (SELECT COUNT(*) FROM ClazzLogAttendanceRecord  " +
            "LEFT JOIN ClazzLog " +
            " ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "WHERE ClazzLog.done = 1 " +
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
            "       WHERE ClazzLog.done = 1 " +
            "           AND ClazzLog.clazzLogClazzUid = :clazzUid " +
            "           AND ClazzLogAttendanceRecord.attendanceStatus = 1   " +
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
            "            WHERE ClazzLog.done = 1 " +
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
    @Query("SELECT 1 FROM Clazz WHERE Clazz.clazzUid = :clazzUid AND (" + ENTITY_LEVEL_PERMISSION_CONDITION1 +
            " :permission" + ENTITY_LEVEL_PERMISSION_CONDITION2 + ")")
    abstract fun personHasPermission(accountPersonUid: Long, clazzUid: Long, permission: Long,
                                     callback: UmCallback<Boolean>)

    @Query("SELECT " + TABLE_LEVEL_PERMISSION_CONDITION1 + " :permission "
            + TABLE_LEVEL_PERMISSION_CONDITION2 + " AS hasPermission")
    abstract fun personHasPermission(accountPersonUid: Long, permission: Long,
                                     callback: UmCallback<Boolean>)

//    @Query("SELECT Clazz.clazzUid as primaryKey, " +
//            "(" + ENTITY_LEVEL_PERMISSION_CONDITION1 +
//            Role.PERMISSION_CLAZZ_UPDATE + ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
//            " AS userCanUpdate " +
//            " FROM Clazz WHERE Clazz.clazzUid in (:primaryKeys)")
//    @UmSyncCheckIncomingCanUpdate
//    abstract fun syncFindExistingEntities(primaryKeys: List<Long>,
//                                          accountPersonUid: Long): List<UmSyncExistingEntity>

    @Query("SELECT COUNT(*) FROM Clazz " +
            "WHERE " +
            "clazzLocalChangeSeqNum > (SELECT syncedToLocalChangeSeqNum FROM SyncStatus WHERE tableId = 6) " +
            "AND clazzLastChangedBy = (SELECT deviceBits FROM SyncDeviceBits LIMIT 1) " +
            "AND ((" + ENTITY_LEVEL_PERMISSION_CONDITION1 + Role.PERMISSION_CLAZZ_UPDATE + //can updateState it

            ENTITY_LEVEL_PERMISSION_CONDITION2 + ") " +
            " OR (" + TABLE_LEVEL_PERMISSION_CONDITION1 +
            Role.PERMISSION_CLAZZ_INSERT + //can insert on table

            TABLE_LEVEL_PERMISSION_CONDITION2 + "))")
    abstract fun countPendingLocalChanges(accountPersonUid: Long): Int

    @Query("SELECT Clazz.*," +
            "Location.timeZone " +
            " FROM Clazz " +
            "LEFT JOIN Location ON Clazz.clazzLocationUid = Location.locationUid " +
            "WHERE " + ENTITY_LEVEL_PERMISSION_CONDITION1
            + Role.PERMISSION_CLAZZ_SELECT + ENTITY_LEVEL_PERMISSION_CONDITION2)
    abstract fun findAllClazzesWithSelectPermission(accountPersonUid: Long): List<ClazzWithTimeZone>

    @Query("SELECT clazzName FROM Clazz WHERE clazzUid = :clazzUid")
    abstract fun getClazzName(clazzUid: Long): String

    @Query("SELECT clazzName FROM Clazz WHERE clazzUid = :clazzUid")
    abstract fun getClazzNameAsync(clazzUid: Long, callback: UmCallback<String>)

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

    companion object {

        const val ENTITY_LEVEL_PERMISSION_CONDITION1 = " (SELECT admin FROM Person WHERE personUid = :accountPersonUid) OR " +
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

        const val TABLE_LEVEL_PERMISSION_CONDITION1 = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid) " +
                "OR " +
                "EXISTS(SELECT PersonGroupMember.groupMemberPersonUid FROM PersonGroupMember " +
                " JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid " +
                " JOIN Role ON EntityRole.erRoleUid = Role.roleUid " +
                " WHERE " +
                " PersonGroupMember.groupMemberPersonUid = :accountPersonUid " +
                " AND EntityRole.erTableId = " + Clazz.TABLE_ID +
                " AND Role.rolePermissions & "

        const val TABLE_LEVEL_PERMISSION_CONDITION2 = " > 0)"

        private const val CLAZZ_WHERE = " SELECT Clazz.*, " +
                "(SELECT ClazzLog.logDate FROM ClazzLog " +
                "WHERE ClazzLog.clazzLogClazzUid = Clazz.clazzUid AND ClazzLog.done = 1 " +
                "ORDER BY ClazzLog.logDate DESC LIMIT 1) AS lastRecorded, " +
                "(SELECT COUNT(*) " +
                " FROM ClazzMember WHERE " +
                " ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                " AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT +
                " AND ClazzMember.clazzMemberActive = 1) AS numStudents, " +
                " (SELECT COUNT(*) FROM ClazzMember " +
                " WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                " AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER +
                " AND ClazzMember.clazzMemberActive = 1 ) AS numTeachers, " +
                " (SELECT GROUP_CONCAT(Person.firstNames || ' ' ||  Person.lastName ) as teacherName " +
                " FROM Person where Person.personUid in (SELECT ClazzMember.clazzMemberPersonUid " +
                " FROM ClazzMember WHERE ClazzMember.role = " + ClazzMember.ROLE_TEACHER +
                " AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid" +
                " AND ClazzMember.clazzMemberActive = 1) " +
                " ) AS teacherNames "

        private const val SELECT_CLAZZ_WHERE_PERMISSION = " SELECT " +
                "   Clazz.*, " +
                "   (SELECT COUNT(*) " +
                "       FROM ClazzMember WHERE " +
                "       ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "       AND ClazzMember.role = " + ClazzMember.ROLE_STUDENT +
                "       AND ClazzMember.clazzMemberActive = 1" +
                "       ) AS numStudents, " +
                "   (SELECT COUNT(*) FROM ClazzMember " +
                "       WHERE ClazzMember.clazzMemberClazzUid = Clazz.clazzUid " +
                "       AND ClazzMember.role = " + ClazzMember.ROLE_TEACHER +
                "       AND ClazzMember.clazzMemberActive = 1 " +
                "   ) AS numTeachers, " +
                "   (SELECT GROUP_CONCAT" +
                "       (Person.firstNames || ' ' ||  Person.lastName ) as teacherName " +
                "       FROM Person where Person.personUid in " +
                "       (SELECT ClazzMember.clazzMemberPersonUid " +
                "           FROM ClazzMember WHERE ClazzMember.role = " + ClazzMember.ROLE_TEACHER +
                "           AND ClazzMember.clazzMemberClazzUid = Clazz.clazzUid" +
                "           AND ClazzMember.clazzMemberActive = 1" +
                "       ) " +
                "   ) AS teacherNames "
    }


}
