package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
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

    @Update
    abstract fun update(entity: Schedule?)

    @Insert
    abstract fun insertAsync(entity: Schedule, resultObject: UmCallback<Long>)

    @Query("SELECT * FROM Schedule")
    abstract fun findAllSchedules(): DataSource.Factory<Int, Schedule>

    @Query("SELECT * FROM SCHEDULE")
    abstract fun findAllSchedulesAsList(): List<Schedule>

    @Update
    abstract fun updateAsync(entity: Schedule, resultObject: UmCallback<Int>)

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUid(uid: Long): Schedule

    @Query("SELECT * FROM Schedule WHERE scheduleUid = :uid")
    abstract fun findByUidAsync(uid: Long, resultObject: UmCallback<Schedule>)

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND scheduleActive = 1")
    abstract fun findAllSchedulesByClazzUid(clazzUid: Long): DataSource.Factory<Int, Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleClazzUid = :clazzUid AND scheduleActive = 1")
    abstract fun findAllSchedulesByClazzUidAsList(clazzUid: Long): List<Schedule>

    fun disableSchedule(scheduleUid: Long) {
        findByUidAsync(scheduleUid, object : UmCallback<Schedule> {
            override fun onSuccess(result: Schedule?) {
                result!!.isScheduleActive = false
                update(result)
            }

            override fun onFailure(exception: Throwable?) {

            }
        })
    }

    @Query("SELECT * FROM DateRange " +
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

    /**
     * Creates ClazzLogs for every clazzes the account person has access to between start and end
     * time.
     *
     * Note: We always create ClazzLogs in the TimeZone.
     * Note 2: the startTime and endTime are times in the phone's timezone.
     *
     * @param startTime             between start time
     * @param endTime               AND end time
     * @param accountPersonUid      The person
     * @param db                    The database
     */
    fun createClazzLogs(startTime: Long, endTime: Long, accountPersonUid: Long, db: UmAppDatabase) {

        //TODO KMP : Calendar in KMP
//        //This method will usually be called from the Workmanager in Android every day. Making the
//        // start time 00:00 and end tim 23:59 : Note: This is the device's timzone. (not class)
//        val startCalendar = Calendar.getInstance()
//        startCalendar.setTimeInMillis(startTime)
//        UMCalendarUtil.normalizeSecondsAndMillis(startCalendar)
//
//        val endCalendar = Calendar.getInstance()
//        endCalendar.setTimeInMillis(endTime)
//        UMCalendarUtil.normalizeSecondsAndMillis(endCalendar)
//
//        val startMsOfDay = ((startCalendar.get(Calendar.HOUR_OF_DAY) * 24 +
//                startCalendar.get(Calendar.MINUTE)) * 60 * 1000).toLong()
//
//        //Get a list of all classes the logged in user has access to:
//        val clazzList = db.clazzDao.findAllClazzesWithSelectPermission(
//                accountPersonUid)
//        //Loop over the classes
//        for (clazz in clazzList) {
//            //Skipp classes that have no time zone
//            if (clazz.timeZone == null) {
//                System.err.println("Warning: cannot create schedules for clazz" +
//                        clazz.clazzName + ", uid:" +
//                        clazz.clazzUid + " as it has no timezone")
//                continue
//            }
//
//            val timeZone = clazz.timeZone
//
//
//            //Get a list of schedules for the classes
//            val clazzSchedules = findAllSchedulesByClazzUidAsList(clazz.clazzUid)
//            for (schedule in clazzSchedules) {
//
//                var incToday = startMsOfDay <= schedule.sceduleStartTime
//                val startTimeMins = schedule.sceduleStartTime / (1000 * 60)
//
//                var nextScheduleOccurence: Calendar? = null
//
//                if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_DAILY) {
//
//                    val tomorrow = Calendar.getInstance()
//                    tomorrow.add(Calendar.DATE, 1)
//                    val tomorrowDay = tomorrow.get(Calendar.DAY_OF_WEEK)
//                    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
//
//                    val dayOfWeek: Int
//                    if (!incToday) {
//                        dayOfWeek = tomorrowDay
//                    } else {
//                        dayOfWeek = today
//                    }
//                    //TODO: Associate with weekend feature in the future
//                    if (dayOfWeek == Calendar.SUNDAY) {
//                        //skip
//                        println("Today is a weekend. Skipping ClazzLog creation for today.")
//
//                    } else if (checkGivenDateAHolidayForClazz(startCalendar.getTimeInMillis(),
//                                    clazz.clazzUid)) {
//                        //Its a holiday. Skip
//                        println("Skipping holiday")
//
//                    } else if (clazz.clazzEndTime != 0L && startCalendar.getTimeInMillis() > clazz.clazzEndTime) {
//                        //Date is ahead of clazz end date. Skipping.
//                        println("Skipping cause current date is after Class's end date.")
//
//                    } else if (clazz.clazzStartTime != 0L && startCalendar.getTimeInMillis() < clazz.clazzStartTime) {
//                        //Date is before Clazz's start date. Skipping
//                        println("Skipping cause current date is before Class's start date.")
//                    } else {
//
//                        //This will get the next schedule for that day. For the same day, it will
//                        //return itself if incToday is set to true, else it will go to next week.
//                        nextScheduleOccurence = UMCalendarUtil.copyCalendarAndAdvanceTo(
//                                startCalendar, timeZone, dayOfWeek, incToday)
//
//                        //Set to 00:00
//                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, 0)
//                        nextScheduleOccurence!!.set(Calendar.MINUTE, 0)
//                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
//                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
//
//                        //Now move it to desired hour:
//                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, (startTimeMins / 60).toInt())
//                        nextScheduleOccurence!!.set(Calendar.MINUTE, (startTimeMins % 60).toInt())
//                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
//                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
//                    }
//
//                } else if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_WEEKLY) {
//
//                    if (checkGivenDateAHolidayForClazz(startCalendar.getTimeInMillis(),
//                                    clazz.clazzUid)) {
//                        //Its a holiday. Skip it.
//                        println("Skipping holiday")
//                    } else if (clazz.clazzEndTime != 0L && startCalendar.getTimeInMillis() > clazz.clazzEndTime) {
//                        //Date is ahead of clazz end date. Skipping.
//                        println("Skipping cause current date is after Class's end date.")
//
//                    } else if (clazz.clazzStartTime != 0L && startCalendar.getTimeInMillis() < clazz.clazzStartTime) {
//                        //Date is before Clazz's start date. Skipping
//                        println("Skipping cause current date is before Class's start date.")
//                    } else {
//
//                        //Will be true if today is schedule day
//                        incToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == schedule.scheduleDay
//
//                        //Get the day of next occurence.
//                        nextScheduleOccurence = UMCalendarUtil.copyCalendarAndAdvanceTo(
//                                startCalendar, timeZone, schedule.scheduleDay, incToday)
//
//                        //Set the day's timezone to Clazz
//                        nextScheduleOccurence!!.setTimeZone(TimeZone.getTimeZone(timeZone))
//
//                        nextScheduleOccurence = UMCalendarUtil.copyCalendarAndAdvanceTo(
//                                startCalendar, clazz.timeZone, schedule.scheduleDay, incToday)
//
//                        //Set to 00:00
//                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, 0)
//                        nextScheduleOccurence!!.set(Calendar.MINUTE, 0)
//                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
//                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
//
//                        //Now move it to desired hour:
//                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, (startTimeMins / 60).toInt())
//                        nextScheduleOccurence!!.set(Calendar.MINUTE, (startTimeMins % 60).toInt())
//                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
//                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
//                    }
//                }
//
//                if (nextScheduleOccurence != null && nextScheduleOccurence!!.before(endCalendar)) {
//                    //this represents an instance of this class that should take place and
//                    //according to the arguments provided, we should check that this instance exists
//                    val logInstanceHash = ClazzLogDao.generateClazzLogUid(clazz.clazzUid,
//                            nextScheduleOccurence!!.getTimeInMillis())
//                    val existingLog = db.getClazzLogDao().findByUid(logInstanceHash)
//
//                    if (existingLog == null || existingLog!!.isCanceled()) {
//                        val newLog = ClazzLog(logInstanceHash.toLong(), clazz.clazzUid,
//                                nextScheduleOccurence!!.getTimeInMillis(), schedule.scheduleUid)
//                        db.getClazzLogDao().replace(newLog)
//                    }
//                }
//            }
//        }
    }

    @Insert
    abstract fun insertScheduledCheck(check: ScheduledCheck)

    /**
     * Used in testing.
     *
     * @param days
     * @param accountPersonUid
     * @param db
     */
    fun createClazzLogsForEveryDayFromDays(days: Int, accountPersonUid: Long,
                                           db: UmAppDatabase) {
        //TODO: KMP Fix
//        for (i in 1..days) {
//            val dayCal = Calendar.getInstance()
//            dayCal.add(Calendar.DATE, -i)
//            dayCal.set(Calendar.HOUR_OF_DAY, 0)
//            dayCal.set(Calendar.MINUTE, 0)
//            dayCal.set(Calendar.SECOND, 0)
//            dayCal.set(Calendar.MILLISECOND, 0)
//            val startTime = dayCal.getTimeInMillis()
//
//            dayCal.set(Calendar.HOUR_OF_DAY, 23)
//            dayCal.set(Calendar.MINUTE, 59)
//            dayCal.set(Calendar.SECOND, 59)
//            dayCal.set(Calendar.MILLISECOND, 999)
//            val endTime = dayCal.getTimeInMillis()
//            createClazzLogs(startTime, endTime, accountPersonUid, db)
//        }
    }

    /**
     * Creates clazzLog for today since clazzlogs are generated for the next day
     * automatically.
     * Called when a new Schedule is created in AddScheduleDialogPresenter , AND
     * Called by ClazzLogScheduleWorker work manager to be run everyday 00:00
     *
     * The method creates ClazzLog from the device's time zone.
     * ie: today is device's 00:00 to device's 23:59.
     */
    fun createClazzLogsForToday(accountPersonUid: Long, db: UmAppDatabase) {

        //TODO: KMP Fix
//        //Note this calendar is created on the device's time zone.
//        val dayCal = Calendar.getInstance()
//        dayCal.set(Calendar.HOUR_OF_DAY, 0)
//        dayCal.set(Calendar.MINUTE, 0)
//        dayCal.set(Calendar.SECOND, 0)
//        dayCal.set(Calendar.MILLISECOND, 0)
//        val startTime = dayCal.getTimeInMillis()
//
//        dayCal.set(Calendar.HOUR_OF_DAY, 23)
//        dayCal.set(Calendar.MINUTE, 59)
//        dayCal.set(Calendar.SECOND, 59)
//        dayCal.set(Calendar.MILLISECOND, 999)
//        val endTime = dayCal.getTimeInMillis()
//        createClazzLogs(startTime, endTime, accountPersonUid, db)
    }

}
