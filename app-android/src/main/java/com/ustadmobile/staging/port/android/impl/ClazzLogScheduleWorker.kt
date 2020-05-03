package com.ustadmobile.staging.port.android.impl

import android.content.Context
import androidx.work.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Schedule
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class is used to schedule a task that runs at midnight every day to create the ClazzLog
 * items for the following day
 */
class ClazzLogScheduleWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        val dbRepo = UmAccountManager.getRepositoryForActiveAccount(applicationContext)
        //Create ClazzLogs for Today (call SchduledDao ) -
        // -> loop over clazzes and schedules and create ClazzLogs

//        dbRepo.scheduleDao.createClazzLogsForToday(
//                UmAccountManager.getActivePersonUid(applicationContext), dbRepo)
        createClazzlogsForToday(dbRepo)

        //Queue next worker at 00:00
        queueClazzLogScheduleWorker(getNextClazzLogScheduleDueTime())
        UstadMobileSystemImpl.instance.scheduleChecks(applicationContext)
        return ListenableWorker.Result.success()
    }

    private fun createClazzlogsForToday(dbRepo:UmAppDatabase){

        //Note this calendar is created on the device's time zone.
        val dayCal = Calendar.getInstance()
        dayCal.set(Calendar.HOUR_OF_DAY, 0)
        dayCal.set(Calendar.MINUTE, 0)
        dayCal.set(Calendar.SECOND, 0)
        dayCal.set(Calendar.MILLISECOND, 0)
        val startTime = dayCal.getTimeInMillis()
//        val startTime = UMCalendarUtil.getToday000000()

        dayCal.set(Calendar.HOUR_OF_DAY, 23)
        dayCal.set(Calendar.MINUTE, 59)
        dayCal.set(Calendar.SECOND, 59)
        dayCal.set(Calendar.MILLISECOND, 999)
        val endTime = dayCal.getTimeInMillis()

        createClazzLogs(startTime, endTime,
                UmAccountManager.getActivePersonUid(applicationContext), dbRepo)
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

        //This method will usually be called from the Workmanager in Android every day. Making the
        // start time 00:00 and end tim 23:59 : Note: This is the device's timzone. (not class)
        var startT: Long = UMCalendarUtil.normalizeSecondsAndMillis(startTime)
        val startCalendar = Calendar.getInstance()
        startCalendar.setTimeInMillis(startT)

        var endT: Long = UMCalendarUtil.normalizeSecondsAndMillis(endTime)
        val endCalendar = Calendar.getInstance()
        endCalendar.setTimeInMillis(endT)

        val startMsOfDay = ((startCalendar.get(Calendar.HOUR_OF_DAY) * 24 +
                startCalendar.get(Calendar.MINUTE)) * 60 * 1000).toLong()

        //Get a list of all classes the logged in user has access to:
        val clazzList = db.clazzDao.findAllClazzesWithSelectPermission(
                accountPersonUid)
        //Loop over the classes
        for (clazz in clazzList) {
            //Skipp classes that have no time zone
            //TODOne: Timezone.: Solution: Almost every clazzz WILL have a timezone
            // by default at least in the Clazz edit page.
//            if (clazz.timeZone == null) {
//                System.err.println("Warning: cannot create schedules for clazz" +
//                        clazz.clazzName + ", uid:" +
//                        clazz.clazzUid + " as it has no timezone")
//                continue
//            }

            var timeZone = clazz.timeZone
            if(timeZone == null){
                timeZone = ""
            }

            //Get a list of schedules for the classes
            val clazzSchedules = db.scheduleDao.findAllSchedulesByClazzUidAsList(clazz.clazzUid)
            for (schedule in clazzSchedules) {

                var incToday = startMsOfDay <= schedule.sceduleStartTime
                val startTimeMins = schedule.sceduleStartTime / (1000 * 60)

                var nextScheduleOccurence: Calendar? = null

                if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_DAILY) {

                    val tomorrow = Calendar.getInstance()
                    tomorrow.add(Calendar.DATE, 1)
                    val tomorrowDay = tomorrow.get(Calendar.DAY_OF_WEEK)
                    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

                    val dayOfWeek: Int
                    if (!incToday) {
                        dayOfWeek = tomorrowDay
                    } else {
                        dayOfWeek = today
                    }
                    //TODO: Associate with weekend feature in the future
                    if (dayOfWeek == Calendar.SUNDAY) {
                        //skip
                        println("Today is a weekend. Skipping ClazzLog creation for today.")

                    } else if (db.scheduleDao.checkGivenDateAHolidayForClazz(startCalendar.getTimeInMillis(),
                                    clazz.clazzUid)) {
                        //Its a holiday. Skip
                        println("Skipping holiday")

                    } else if (clazz.clazzEndTime != 0L && startCalendar.getTimeInMillis() > clazz.clazzEndTime) {
                        //Date is ahead of clazz end date. Skipping.
                        println("Skipping cause current date is after Class's end date.")

                    } else if (clazz.clazzStartTime != 0L && startCalendar.getTimeInMillis() < clazz.clazzStartTime) {
                        //Date is before Clazz's start date. Skipping
                        println("Skipping cause current date is before Class's start date.")
                    } else {

                        //This will get the next schedule for that day. For the same day, it will
                        //return itself if incToday is set to true, else it will go to next week.
                        nextScheduleOccurence = copyCalendarAndAdvanceTo(
                                startCalendar, dayOfWeek, incToday)

                        //Set to 00:00
                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, 0)
                        nextScheduleOccurence!!.set(Calendar.MINUTE, 0)
                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)

                        //Now move it to desired hour:
                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, (startTimeMins / 60).toInt())
                        nextScheduleOccurence!!.set(Calendar.MINUTE, (startTimeMins % 60).toInt())
                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
                    }

                } else if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_WEEKLY) {

                    if (db.scheduleDao.checkGivenDateAHolidayForClazz(startCalendar.getTimeInMillis(),
                                    clazz.clazzUid)) {
                        //Its a holiday. Skip it.
                        println("Skipping holiday")
                    } else if (clazz.clazzEndTime != 0L && startCalendar.getTimeInMillis() > clazz.clazzEndTime) {
                        //Date is ahead of clazz end date. Skipping.
                        println("Skipping cause current date is after Class's end date.")

                    } else if (clazz.clazzStartTime != 0L && startCalendar.getTimeInMillis() < clazz.clazzStartTime) {
                        //Date is before Clazz's start date. Skipping
                        println("Skipping cause current date is before Class's start date.")
                    } else {

                        //Will be true if today is schedule day
                        incToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == schedule.scheduleDay

                        //Get the day of next occurence.
                        nextScheduleOccurence = copyCalendarAndAdvanceTo(
                                startCalendar, schedule.scheduleDay, incToday)

                        //Set the day's timezone to Clazz
                        nextScheduleOccurence!!.setTimeZone(TimeZone.getTimeZone(timeZone))

                        nextScheduleOccurence = copyCalendarAndAdvanceTo(
                                startCalendar, schedule.scheduleDay, incToday)

                        //Set to 00:00
                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, 0)
                        nextScheduleOccurence!!.set(Calendar.MINUTE, 0)
                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)

                        //Now move it to desired hour:
                        nextScheduleOccurence!!.set(Calendar.HOUR_OF_DAY, (startTimeMins / 60).toInt())
                        nextScheduleOccurence!!.set(Calendar.MINUTE, (startTimeMins % 60).toInt())
                        nextScheduleOccurence!!.set(Calendar.SECOND, 0)
                        nextScheduleOccurence!!.set(Calendar.MILLISECOND, 0)
                    }
                }

                if (nextScheduleOccurence != null && nextScheduleOccurence!!.before(endCalendar)) {
                    //this represents an instance of this class that should take place and
                    //according to the arguments provided, we should check that this instance exists
                    val logInstanceHash = ClazzLogDao.generateClazzLogUid(clazz.clazzUid,
                            nextScheduleOccurence!!.getTimeInMillis())
                    val existingLog = db.clazzLogDao.findByUid(logInstanceHash.toLong())

                    if (existingLog == null || existingLog!!.clazzLogCancelled) {
                        val newLog = ClazzLog(logInstanceHash.toLong(), clazz.clazzUid,
                                nextScheduleOccurence!!.getTimeInMillis(), schedule.scheduleUid)
                        db.clazzLogDao.replace(newLog)
                    }
                }
            }
        }
    }


    /**
     * Advance a calendar to the next occurence of a particular day (e.g. Monday, Tuesday, etc).
     *
     * @param calendar the calendar to use as the start time
     * @param dayOfWeek the day of the week to go to as per Calendar constants
     * @param incToday if true, then if the start date matches the end date, make no changes. If false,
     * and the input calendar is already on the same day of the week, then return 7
     *
     * @return A new calendar instance advanced to the next occurence of the given day of the week
     */
    private fun copyCalendarAndAdvanceTo(calendar: Calendar, dayOfWeek: Int, incToday: Boolean): Calendar {

        //Note: calendar is the calendar in the phone's time zone. The phone's timezone can be
        // different from the Class's timezone. Since all times are in the Class's time zone,
        // a phone 9 am is in fact intended to be Class TimeZone's 9 am.
        //
        // The return Calendar is the calendar where the next occurence should be. This should
        // match with the right day of the week. Hence this has to be in the Local time zone.
        // (ie: to avoid situations where next occurence clazz timezone = previous day device.
        // Since theis method is called every midnight of the phone device, we need the time to be
        // the right day (ie phone device's timezone). For this purpose we will advance to the phone
        // timezone and can set its timezone to Clazz outside this method.

        val comparisonCalendar = Calendar.getInstance()
        comparisonCalendar.timeInMillis = calendar.timeInMillis
        comparisonCalendar.timeZone = calendar.timeZone

        val today = calendar.get(Calendar.DAY_OF_WEEK)

        if (today == dayOfWeek) {
            if (!incToday) {
                comparisonCalendar.timeInMillis = calendar.timeInMillis + 7 * 1000 * 60 * 60 * 24
            }


            //Addition:
            // Calendar without Time Zone's day = time zoned calendar's day = expected day of week
            if (comparisonCalendar.get(Calendar.DAY_OF_WEEK) === calendar.get(Calendar.DAY_OF_WEEK)
                    && calendar.get(Calendar.DAY_OF_WEEK) === dayOfWeek) {
                return comparisonCalendar
            }
            comparisonCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            return comparisonCalendar
        }


        val deltaDays: Int
        if (dayOfWeek > today) {
            deltaDays = dayOfWeek - today
        } else {
            deltaDays = 7 - today + dayOfWeek
        }

        comparisonCalendar.timeInMillis = calendar.timeInMillis + deltaDays * 1000 * 60 * 60 * 24

        return comparisonCalendar
    }

    companion object {

        var TAG = "ClazzLogSchedule"

        fun queueClazzLogScheduleWorker(time: Long) {
            val request = OneTimeWorkRequest.Builder(ClazzLogScheduleWorker::class.java)
                    .setInitialDelay(time - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addTag(TAG)
                    .build()
            WorkManager.getInstance().enqueue(request)
        }

        /**
         * Determine when we should next generate ClazzLog items for any classes of the active user.
         * This is at exactly midnight.
         *
         * @return
         */
        fun getNextClazzLogScheduleDueTime(): Long {
            val nextTimeCal = Calendar.getInstance()
            nextTimeCal.timeInMillis = System.currentTimeMillis() + 1000 * 60 * 60 * 24
            nextTimeCal.set(Calendar.HOUR_OF_DAY, 0)
            nextTimeCal.set(Calendar.MINUTE, 0)
            nextTimeCal.set(Calendar.SECOND, 0)
            nextTimeCal.set(Calendar.MILLISECOND, 0)
            return nextTimeCal.timeInMillis
        }


    }
}
