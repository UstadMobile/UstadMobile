package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao(permissionJoin = "LEFT JOIN ClazzLog ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " 
        + "LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid ", 
        selectPermissionCondition = ClazzDao.Companion.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzLogAttendanceRecordDao : BaseDao<ClazzLogAttendanceRecord> {

    @Insert
    abstract override fun insert(entity: ClazzLogAttendanceRecord): Long

    @Insert
    abstract suspend fun insertListAsync(entities: List<ClazzLogAttendanceRecord>): Array<Long>

    @Query("SELECT * from ClazzLogAttendanceRecord WHERE clazzLogAttendanceRecordUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLogAttendanceRecord?

    @Update
    abstract suspend fun updateListAsync(entities: List<ClazzLogAttendanceRecord>)


    @Query("""SELECT ClazzLogAttendanceRecord.*, Person.*
         FROM ClazzLogAttendanceRecord 
         LEFT JOIN ClazzMember ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid
         LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid
         WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid""")
    abstract fun findByClazzLogUid(clazzLogUid: Long): List<ClazzLogAttendanceRecordWithPerson>

    class AttendanceByThresholdRow {

        var age: Int = 0

        var totalLowAttendanceMale: Int = 0

        var totalLowAttendanceFemale: Int = 0

        var totalMediumAttendanceMale: Int = 0

        var totalMediumAttendanceFemale: Int = 0

        var totalHighAttendanceMale: Int = 0

        var totalHighAttendanceFemale: Int = 0
    }

    @Query("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "  " +
            "  " +
            " " +
            " " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid " +
            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "    " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            "    " +
            "    " +
            " ) numSessionsTbl " +
            " " +
            " " +
            " " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    abstract suspend fun getAttendanceGroupedByThresholds(datetimeNow: Long, fromTime: Long,
                                                          toTime: Long, lowAttendanceThreshold: Float,
                                                          midAttendanceThreshold: Float)
                                                :List<AttendanceResultGroupedByAgeAndThreshold>

    @Query("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid " +
            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    abstract suspend fun getAttendanceGroupedByThresholdsWithClazz(datetimeNow: Long, fromTime: Long,
                                                                   toTime: Long, lowAttendanceThreshold: Float,
                                                                   midAttendanceThreshold: Float,
                                                                   clazzes: List<Long>):
            List<AttendanceResultGroupedByAgeAndThreshold>

    @Query("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid, " +

            "   locationUid " +         //added


            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            "   LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +       //added

            "   LEFT JOIN Location ON Location.locationUid = Clazz.clazzLocationUid " + //added


            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "    AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE numSessionsTbl.locationUid = :locationUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    abstract suspend fun getAttendanceGroupedByThresholdsWithClazzAndLocation(datetimeNow: Long, fromTime: Long,
                                                                              toTime: Long, lowAttendanceThreshold: Float,
                                                                              midAttendanceThreshold: Float, clazzes: List<Long>,
                                                                              locationUid: Long)
            : List<AttendanceResultGroupedByAgeAndThreshold>

    @Query("select  " +
            " count(DISTINCT Person.personUid) as total, " +
            " Person.gender, " +
            " cast((:datetimeNow - Person.dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000) as int) as age, " +
            " CASE  " +
            "  WHEN numSessionsTbl.attendancePercentage < :lowAttendanceThreshold THEN \"LOW\" " +
            "  WHEN numSessionsTbl.attendancePercentage < :midAttendanceThreshold THEN \"MEDIUM\" " +
            "  ELSE \"HIGH\" " +
            " END thresholdGroup " +
            "FROM  " +
            " ( " +
            "  SELECT  " +
            "   cast( SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " THEN 1 ELSE 0 END) as float) / COUNT(*) as attendancePercentage, " +
            "   ClazzLogAttendanceRecordClazzLogUid, " +
            "   clazzLogAttendanceRecordClazzMemberUid, " +

            "   locationUid " +         //added


            "   FROM ClazzLogAttendanceRecord as numSessions  " +
            "   LEFT JOIN ClazzLog on ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            "   LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +       //added

            "   LEFT JOIN Location ON Location.locationUid = Clazz.clazzLocationUid " + //added


            "   WHERE ClazzLog.logDate > :fromTime AND ClazzLog.logDate < :toTime " +
            "   GROUP BY clazzLogAttendanceRecordClazzMemberUid " +
            " ) numSessionsTbl " +
            "LEFT JOIN ClazzLog ON " +
            " numSessionsTbl.ClazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
            "LEFT JOIN ClazzLogAttendanceRecord ON " +
            " numSessionsTbl.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
            "LEFT JOIN ClazzMember on " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            "LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE numSessionsTbl.locationUid = :locationUid " +
            "GROUP BY Person.gender, age, thresholdGroup " +
            " ORDER BY age, thresholdGroup ")
    abstract suspend  fun getAttendanceGroupedByThresholdsWithLocation(datetimeNow: Long, fromTime: Long,
                                                                       toTime: Long, lowAttendanceThreshold: Float,
                                                                       midAttendanceThreshold: Float,
                                                                       locationUid: Long):
    List<AttendanceResultGroupedByAgeAndThreshold>

    suspend fun getAttendanceGroupedByThresholdsAndClasses(datetimeNow: Long, fromTime: Long,
                                                   toTime: Long, lowAttendanceThreshold: Float, midAttendanceThreshold: Float,
                                                   clazzes: List<Long>):
    List<AttendanceResultGroupedByAgeAndThreshold> {
        if (clazzes.isEmpty()) {
            return getAttendanceGroupedByThresholds(datetimeNow, fromTime, toTime,
                    lowAttendanceThreshold,
                    midAttendanceThreshold)
        } else {
            return getAttendanceGroupedByThresholdsWithClazz(datetimeNow, fromTime, toTime,
                    lowAttendanceThreshold,
                    midAttendanceThreshold, clazzes)
        }
    }

    @Query("SELECT ClazzLogAttendanceRecord.* , Person.* " +
            " FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzMember " +
            " on ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = " +
            " ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person on ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            " AND CAST(Person.active AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT )
    abstract fun findAttendanceRecordsWithPersonByClassLogId(clazzLogUid: Long): DataSource.Factory<Int, ClazzLogAttendanceRecordWithPerson>

    @Query("SELECT ClazzMember.clazzMemberUid FROM ClazzMember WHERE " +
            " ClazzMember.clazzMemberClazzUid = :clazzId " +
            " AND CAST(ClazzMember.clazzMemberActive AS INTEGER) = 1 " +
            " AND ClazzMember.clazzMemberRole = " + ClazzMember.ROLE_STUDENT +
            " AND ClazzMember.clazzMemberClazzUid " +
            " EXCEPT " +
            "SELECT clazzLogAttendanceRecordClazzMemberUid FROM ClazzLogAttendanceRecord " +
            " WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid")
    abstract suspend fun findPersonUidsWithNoClazzAttendanceRecord(clazzId: Long,
                                           clazzLogUid: Long) :List<Long>


    @Query(QUERY_ATTENDANCE_NUMBERS_FOR_CLASS_BY_DATE)
    @Deprecated("Used only in staging code")
    abstract suspend fun findDailyAttendanceByClazzUidAndDateAsync(clazzUid: Long, fromDate: Long,
                                   toDate: Long):List<DailyAttendanceNumbers>

    @Query("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED + " and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED + " and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    abstract suspend fun findOverallDailyAttendanceNumbersByDateAndStuff(fromDate: Long,
                                                                 toDate: Long, clazzes: List<Long>)
            : List<DailyAttendanceNumbers>


    @Query("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLog.clazzLogDone = 1 " +
            " AND Clazz.clazzLocationUid IN (:locations)  " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    abstract suspend fun findOverallDailyAttendanceNumbersByDateAndLocation(fromDate: Long,
                                                                    toDate: Long, locations:
                                                                                List<Long>)
            :List<DailyAttendanceNumbers>

    @Query("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 0.5 / SUM(CASE WHEN clazzMember.clazzMemberRole = 1 THEN 1 else 0 end) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / SUM(CASE WHEN person.gender = 1 AND clazzMember.clazzMemberRole = 1 THEN 1 else 0 end) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/ SUM(CASE WHEN person.gender = 2 AND clazzMember.clazzMemberRole = 1 THEN 1 else 0 end) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE ClazzLog.clazzLogDone = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    abstract suspend fun findOverallDailyAttendanceNumbersByDate(fromDate: Long,
                                                                 toDate: Long):
    List<DailyAttendanceNumbers>

    /**
     * @param fromDate  from date
     * @param toDate    to date
     * @param clazzes   list of classes
     * @param locations list of locations
     */
    suspend fun findOverallDailyAttendanceNumbersByDateAndStuff(fromDate: Long, toDate: Long,
                                                        clazzes: List<Long>, locations:
    List<Long>) : List<DailyAttendanceNumbers> {
        if (clazzes.isEmpty()) {
            if (locations.isEmpty()) {
                return findOverallDailyAttendanceNumbersByDate(fromDate, toDate)
            } else {
                return findOverallDailyAttendanceNumbersByDateAndLocation(fromDate, toDate,
                        locations)
            }
        } else {
            if (locations.isEmpty()) {
                return findOverallDailyAttendanceNumbersByDateAndStuff(fromDate, toDate, clazzes)
            } else {
                return findOverallDailyAttendanceNumbersByDateAndClazzesAndLocations(fromDate,
                        toDate,clazzes, locations)
            }
        }
    }


    @Query("select ClazzLogAttendanceRecordClazzLogUid as clazzLogUid, " +
            " ClazzLog.logDate, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED +
            " then 1 else 0 end) * 1.0 / COUNT(*) as attendancePercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT +
            " then 1 else 0 end) * 1.0 / COUNT(*) as absentPercentage, " +
            " sum(case when attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL +
            " then 1 else 0 end) * 1.0 / COUNT(*) as partialPercentage, " +
            " ClazzLog.clazzLogClazzUid as clazzUid, " +
            " sum(case when attendanceStatus = 1 and Person.gender = " + Person.GENDER_FEMALE +
            " then 1 else 0 end) * 1.0 / COUNT(*) as femaleAttendance, " +
            " sum(case when attendanceStatus = 1 and Person.gender =  " + Person.GENDER_MALE +
            " then 1 else 0 end) * 1.0/COUNT(*) as maleAttendance, " +
            " ClazzLog.clazzLogUid as clazzLogUid " +
            " from ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzLog ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +

            " LEFT JOIN ClazzMember ON " +
            " ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid = ClazzMember.clazzMemberUid " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid " +
            " LEFT JOIN Person ON ClazzMember.clazzMemberPersonUid = Person.personUid " +
            " WHERE CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.clazzLogClazzUid IN (:clazzes) " +
            " AND Clazz.clazzLocationUid IN (:locations)  " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            "group by (ClazzLog.logDate)")
    abstract suspend fun findOverallDailyAttendanceNumbersByDateAndClazzesAndLocations(fromDate: Long,
                                                                                       toDate: Long, clazzes: List<Long>, locations: List<Long>)
            : List<DailyAttendanceNumbers>

    /**
     * Checks for ClazzMembers not in a particular Clazz that are not part of the
     * ClazzLogAttendanceRecord and creates their ClazzLogAttendanceRecords.
     *
     * @param clazzId
     * @param clazzLogUid
     */
    suspend fun insertAllAttendanceRecords(clazzId: Long, clazzLogUid: Long) : Array<Long>? {
        val result = findPersonUidsWithNoClazzAttendanceRecord(clazzId, clazzLogUid)
        if(result.isEmpty()) {
            return null
        } else {
            val toInsert = ArrayList<ClazzLogAttendanceRecord>()
            for (clazzMemberUid in result) {
                val record = ClazzLogAttendanceRecord()
                record.clazzLogAttendanceRecordClazzLogUid = clazzLogUid
                record.clazzLogAttendanceRecordClazzMemberUid = clazzMemberUid
                toInsert.add(record)
            }

            return insertListAsync(toInsert)
        }

    }

    @Query("SELECT " +
            " Clazz.clazzName, Clazz.clazzUid as clazzUid, Person.firstNames, Person.lastName, Person.personUid, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ATTENDED + " THEN 1 ELSE 0 END) as daysPresent, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_ABSENT + " THEN 1 ELSE 0 END) as daysAbsent, " +
            " SUM(CASE WHEN attendanceStatus = " + ClazzLogAttendanceRecord.STATUS_PARTIAL + " THEN 1 ELSE 0 END) as daysPartial, " +
            " COUNT(*) as clazzDays, " +
            " ClazzMember.clazzMemberDateLeft as dateLeft, " +
            " ClazzMember.clazzMemberActive as isClazzMemberActive,  " +
            " Person.gender, " +
            " Person.dateOfBirth " +
            " FROM ClazzLogAttendanceRecord " +
            " LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = clazzLogAttendanceRecordClazzMemberUid " +
            " LEFT JOIN Person ON Person.personUid = ClazzMember.clazzMemberPersonUid " +
            " LEFT JOIN ClazzLog ON ClazzLog.clazzLogUid = clazzLogAttendanceRecordClazzLogUid " +
            " LEFT JOIN Clazz ON Clazz.clazzUid = ClazzLog.clazzLogClazzUid " +
            " WHERE " +
            " CAST(ClazzLog.clazzLogDone AS INTEGER) = 1 " +
            " AND ClazzLog.logDate > :fromDate " +
            " AND ClazzLog.logDate < :toDate " +
            " GROUP BY clazzMemberUid " +
            " ORDER BY clazzName ")
    abstract suspend fun findMasterReportDataForAllAsync(fromDate: Long, toDate: Long) :List<ReportMasterItem>

    @Query("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordClazzLogUid = :clazzLogUid AND " +
            "attendanceStatus != :attendanceStatus")
    abstract suspend fun updateAllByClazzLogUid(clazzLogUid: Long, attendanceStatus: Int): Int


    @Query("UPDATE ClazzLogAttendanceRecord SET attendanceStatus = :attendanceStatus " +
            "WHERE clazzLogAttendanceRecordUid = :clazzLogAttendanceRecordUid AND " +
            " attendanceStatus != :attendanceStatus")
    abstract suspend fun updateAttendanceStatus(clazzLogAttendanceRecordUid: Long,
                                        attendanceStatus: Int) : Int

    @Query("SELECT COUNT(*) FROM ClazzLogAttendanceRecord " +
            "where clazzLogAttendanceRecordClazzLogUid = :clazzLogUid " +
            "AND attendanceStatus = :attendanceStatus")
    abstract fun getAttedanceStatusCount(clazzLogUid: Long, attendanceStatus: Int): Int

    companion object {


        const val QUERY_ATTENDANCE_NUMBERS_FOR_CLASS_BY_DATE = " SELECT " +
                "  clazzlog.clazzloguid AS clazzLogUid,  clazzlog.logdate, " +
                "  (  SELECT ( SUM(CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = 1 THEN 1 ELSE 0 END) " +
                "      *1.0/Count(*)   )  " +
                "    FROM ClazzLogAttendanceRecord WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "  ) AS attendancePercentage, " +
                "  (  SELECT ( SUM(CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = 2 THEN 1 ELSE 0 END) " +
                "      *1.0/Count(*)   )  " +
                "      FROM ClazzLogAttendanceRecord WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "  ) AS absentPercentage, " +
                "  (  SELECT (  SUM(CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = 4 THEN 1 ELSE 0 END) " +
                "      *1.0/Count(*) )  " +
                "    FROM ClazzLogAttendanceRecord WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "  ) AS partialPercentage," +
                "  (  SELECT (  SUM(CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = 1 AND person.gender = " + Person.GENDER_FEMALE + " THEN 1 ELSE 0 END) " +
                "      *1.0/Count(*)  )  " +
                "    FROM ClazzLogAttendanceRecord " +
                "    LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
                "    LEFT JOIN person ON clazzmember.clazzmemberpersonuid = person.personuid " +
                "    WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "  ) AS femaleAttendance, " +
                "  (  SELECT ( SUM(CASE WHEN ClazzLogAttendanceRecord.attendanceStatus = 1 AND person.gender = " + Person.GENDER_MALE + " THEN 1 ELSE 0 END) " +
                "      *1.0/Count(*) )  " +
                "    FROM ClazzLogAttendanceRecord  " +
                "    LEFT JOIN ClazzMember ON ClazzMember.clazzMemberUid = ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzMemberUid " +
                "    LEFT JOIN person ON clazzmember.clazzmemberpersonuid = person.personuid " +
                "    WHERE ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "  ) AS maleAttendance,  " +
                "  ( :clazzUid  ) AS clazzUid " +
                " FROM ClazzLog  " +
                "  LEFT JOIN ClazzLogAttendanceRecord ON ClazzLogAttendanceRecord.clazzLogAttendanceRecordClazzLogUid = ClazzLog.clazzLogUid " +
                "WHERE  clazzlog.clazzlogclazzuid = :clazzUid " +
                "   AND ClazzLog.logDate > :fromDate " +
                "   AND ClazzLog.logDate < :toDate " +
                "GROUP  BY ( clazzlog.logdate )  "

    }

}
