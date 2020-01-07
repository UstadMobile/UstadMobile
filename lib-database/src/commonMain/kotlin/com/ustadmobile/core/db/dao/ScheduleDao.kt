package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.DateRange
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScheduledCheck


@UmDao(inheritPermissionFrom = ClazzDao::class, 
        inheritPermissionForeignKey = "scheduleClazzUid", 
        inheritPermissionJoinedPrimaryKey = "clazzUid")
@UmRepository
@Dao
abstract class ScheduleDao : BaseDao<Schedule> {

    @Insert
    abstract override fun insert(entity: Schedule): Long

    @Query("SELECT * FROM Schedule")
    abstract fun findAllSchedules(): DataSource.Factory<Int, Schedule>

    @Query("SELECT * FROM SCHEDULE")
    abstract fun findAllSchedulesAsList(): List<Schedule>

    @Update
    abstract suspend fun updateAsync(entity: Schedule) : Int

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long) : Schedule?

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): DataSource.Factory<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND CAST(scheduleActive AS INTEGER) = 1 ")
    abstract suspend fun findAllSchedulesByClazzUidAsync(clazzUid: Long): List<Schedule>

    suspend fun disableSchedule(scheduleUid: Long) {
        val result = findByUidAsync(scheduleUid)
        result!!.scheduleActive = false
        update(result)

    }

    @Query("SELECT DateRange.* FROM DateRange " +
            " LEFT JOIN Clazz ON Clazz.clazzUid = :clazzUid " +
            " WHERE DateRange.dateRangeUMCalendarUid = Clazz.clazzHolidayUMCalendarUid ")
    abstract fun findAllHolidayDateRanges(clazzUid: Long): List<DateRange>

    /**
     * Checks if a given date is a holiday in the clazz uid specified.
     * @param checkDate The date to check if its a holiday
     * @param clazzUid  The clazz to check for's clazzUid
     * @return  true if it is a holiday, false if not.
     */
    fun checkGivenDateAHolidayForClazz(checkDate: Long, clazzUid: Long): Boolean {
        //1. Get all date ranges for the given clazz day
        val holidays = findAllHolidayDateRanges(clazzUid)
        for (everyHoliday in holidays) {
            //2. Null checkDate's year even if its not present TODO
            val fromDate = everyHoliday.dateRangeFromDate
            val toDate = everyHoliday.dateRangeToDate
            //3. Null year in fromDate and toDate
            //3. Compare
            return if (toDate != 0L) {
                checkDate >= fromDate && checkDate <= toDate
            } else {
                checkDate == fromDate
            }
        }

        return false
    }


    @Insert
    abstract fun insertScheduledCheck(check: ScheduledCheck)


}
