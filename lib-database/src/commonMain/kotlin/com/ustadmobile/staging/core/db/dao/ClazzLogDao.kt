package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes
import com.ustadmobile.lib.db.entities.Role
import kotlinx.serialization.Serializable


@UmDao(permissionJoin = "INNER JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid", 
        selectPermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_SELECT + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        updatePermissionCondition = ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_UPDATE + ClazzDao.ENTITY_LEVEL_PERMISSION_CONDITION2, 
        insertPermissionCondition = ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION1 +
        Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT + ClazzDao.TABLE_LEVEL_PERMISSION_CONDITION2)
@UmRepository
@Dao
abstract class ClazzLogDao : BaseDao<ClazzLog> {

    @Serializable
    class NumberOfDaysClazzesOpen {
        var date: Long = 0
        var number: Int = 0
    }

    /**
     * Small POJO used by the attendance screen to get a list of valid dates for the class (to show
     * in a list) and their UID so they can be looked up.
     */
    @Serializable
    class ClazzLogUidAndDate {

        var clazzLogUid: Long = 0

        var logDate: Long = 0
        constructor()

        constructor(clazzLog: ClazzLog) {
            this.clazzLogUid = clazzLog.clazzLogUid
            this.logDate = clazzLog.logDate
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null ) return false

            val that = o as ClazzLogUidAndDate?

            return if (clazzLogUid != that!!.clazzLogUid) false else logDate == that.logDate
        }

        override fun hashCode(): Int {
            var result = (clazzLogUid xor clazzLogUid.ushr(32)).toInt()
            result = 31 * result + (logDate xor logDate.ushr(32)).toInt()
            return result
        }
    }

    @Insert
    abstract override fun insert(entity: ClazzLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replace(entity: ClazzLog): Long

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUid(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): ClazzLog?

    @Query("SELECT * FROM ClazzLog WHERE clazzLogUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<ClazzLog?>

    @Query("SELECT * FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid ORDER BY logDate DESC LIMIT 1")
    @Deprecated("Used only in staging code")
    abstract suspend fun findMostRecentByClazzUid(clazzUid: Long) : ClazzLog?

    @Query("UPDATE ClazzLog SET clazzLogDone = 1 where clazzLogUid = :clazzLogUid ")
    abstract suspend fun updateDoneForClazzLogAsync(clazzLogUid: Long) : Int

    @Query("SELECT ClazzLog.*, Schedule.sceduleStartTime, Schedule.scheduleEndTime, " +
            "Schedule.scheduleFrequency FROM ClazzLog " +
            "LEFT JOIN Schedule ON Schedule.scheduleUid = ClazzLog.clazzLogScheduleUid " +
            "WHERE clazzLogClazzUid = :clazzUid AND NOT clazzLogCancelled ORDER BY logDate ASC")
    @Deprecated("Used only in staging code")
    abstract fun findByClazzUidNotCancelledWithSchedule(clazzUid: Long): DataSource.Factory<Int,
            ClazzLogWithScheduleStartEndTimes>

    @Query("""SELECT ClazzLog.* FROM ClazzLog WHERE clazzLogClazzUid = :clazzUid
        ORDER BY ClazzLog.logDate DESC""")
    abstract fun findByClazzUidAsFactory(clazzUid: Long): DataSource.Factory<Int, ClazzLog>


    @Query("UPDATE ClazzLog SET clazzLogNumPresent = :clazzLogNumPresent,  " +
            "clazzLogNumAbsent = :numAbsent, " + "clazzLogNumPartial = :numPartial " +
            "WHERE clazzLogUid = :clazzLogUid")
    abstract suspend fun updateClazzAttendanceNumbersAsync(clazzLogUid: Long, clazzLogNumPresent: Int,
                                                   numAbsent: Int, numPartial: Int) : Int

    @Query("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            " GROUP BY ClazzLog.logDate")
    abstract suspend fun getNumberOfClassesOpenForDate(fromDate: Long, toDate: Long) :
        List<NumberOfDaysClazzesOpen>

    @Query("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND ClazzLog.clazzLogClazzUid in (:clazzes) " +
            " GROUP BY ClazzLog.logDate")
    abstract suspend fun getNumberOfClassesOpenForDateClazzes(fromDate: Long, toDate: Long,
                                                      clazzes: List<Long>) :
    List<NumberOfDaysClazzesOpen>

    @Query("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND Clazz.clazzLocationUid in (:locations) " +
            " GROUP BY ClazzLog.logDate")
    abstract suspend fun getNumberOfClassesOpenForDateLocations(fromDate: Long, toDate: Long,
                                    locations: List<Long> ) :List<NumberOfDaysClazzesOpen>

    @Query("SELECT COUNT(Clazz.clazzName) as number, clazzLog.logDate as date from ClazzLog " +
            " LEFT JOIN Clazz ON ClazzLog.clazzLogClazzUid = Clazz.clazzUid" +
            "   WHERE ClazzLog.logDate > :fromDate and ClazzLog.logDate < :toDate " +
            "       AND ClazzLog.clazzLogClazzUid in (:clazzes) " +
            "       AND Clazz.clazzLocationUid in (:locations) " +
            " GROUP BY ClazzLog.logDate")
    abstract suspend fun getNumberOfClassesOpenForDateClazzesLocation(fromDate: Long, toDate: Long,
                              clazzes: List<Long>, locations: List<Long>)
            : List<NumberOfDaysClazzesOpen>

    suspend fun getNumberOfClassesOpenForDateClazzes(fromDate: Long, toDate: Long,
                                             clazzes: List<Long>, locations: List<Long>) :
            List<NumberOfDaysClazzesOpen> {
        if (locations.isEmpty()) {
            if (clazzes.isEmpty()) {
                return getNumberOfClassesOpenForDate(fromDate, toDate)
            } else {
                return getNumberOfClassesOpenForDateClazzes(fromDate, toDate, clazzes)
            }
        } else {
            if (clazzes.isEmpty()) {
                return getNumberOfClassesOpenForDateLocations(fromDate, toDate, locations)
            } else {
                return getNumberOfClassesOpenForDateClazzesLocation(fromDate, toDate, clazzes,
                        locations)
            }
        }

    }

    @Query("UPDATE ClazzLog SET clazzLogCancelled = :clazzLogCancelled WHERE clazzLogScheduleUid = :scheduleUid AND logDate >= :after ")
    abstract fun cancelFutureInstances(scheduleUid: Long, after: Long, clazzLogCancelled: Boolean)


    @Query("SELECT ClazzLog.clazzLogUid, ClazzLog.logDate FROM ClazzLog " +
            " WHERE clazzLogClazzUid = :clazzUid ORDER BY logDate ASC")
    abstract suspend fun getListOfClazzLogUidsAndDatesForClazz(clazzUid: Long)
            :List<ClazzLogUidAndDate>


    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
    """)
    abstract fun findByClazzUidWithinTimeRange(clazzUid: Long, fromTime: Long, toTime: Long): List<ClazzLog>

    @Query("""SELECT ClazzLog.* FROM ClazzLog 
        WHERE 
        ClazzLog.clazzLogClazzUid = :clazzUid 
        AND ClazzLog.logDate BETWEEN :fromTime AND :toTime
        AND (:statusFilter = 0 OR ClazzLog.clazzLogStatusFlag = :statusFilter)
    """)
    abstract fun findByClazzUidWithinTimeRangeLive(clazzUid: Long, fromTime: Long, toTime: Long, statusFilter: Int): DoorLiveData<List<ClazzLog>>

    companion object {


        /**
         * As the ClazzLog object is added using a timer, we need to ensure that the object created for
         * a specific time should come with the same primary key. For this purposes, we generate a
         * a hashcode using the clazzuid and startTime.
         *
         * @param clazzUid UID of the clazz
         * @param startTime scheduled start time of this instance of the clazz
         * @return a hashcode computed from the above
         */
        fun generateClazzLogUid(clazzUid: Long, startTime: Long): Int {
            var hash = clazzUid.hashCode()
            hash = 31 * hash + startTime.hashCode()
            return hash
        }
    }

}
