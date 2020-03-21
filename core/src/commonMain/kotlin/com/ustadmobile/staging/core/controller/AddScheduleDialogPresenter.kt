package com.ustadmobile.core.controller


import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzLogDao
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.db.dao.ScheduledCheckDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.AddScheduleDialogView
import com.ustadmobile.core.view.AddScheduleDialogView.Companion.EVERY_DAY_SCHEDULE_POSITION
import com.ustadmobile.core.view.ClazzEditView.Companion.ARG_SCHEDULE_UID
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.ScheduledCheck
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


class AddScheduleDialogPresenter
/**
 * Initialises all Daos, gets all needed arguments and creates a schedule if argument not given.
 * Updates the schedule to the view.
 * @param context       Context of application
 * @param arguments     Arguments
 * @param view          View
 */
(context: Any, arguments: Map<String, String>?, view: AddScheduleDialogView,
        val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        UstadBaseController<AddScheduleDialogView>(context, arguments!!, view) {

    private var currentSchedule: Schedule? = null

    private val scheduleDao: ScheduleDao

    private val scheduleCheckDao : ScheduledCheckDao
    private val appDatabaseRepo: UmAppDatabase

    internal var currentClazzUid: Long = 0
    private var currentScheduleUid = 0L

    init {

        appDatabaseRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        scheduleDao = appDatabaseRepo.scheduleDao
        scheduleCheckDao = appDatabaseRepo.scheduledCheckDao

        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_SCHEDULE_UID)) {
            currentScheduleUid = arguments!!.get(ARG_SCHEDULE_UID)!!.toLong()
        }

        if (currentScheduleUid > 0) {
            GlobalScope.launch {
                val result = scheduleDao.findByUidAsync(currentScheduleUid)
                currentSchedule = result
                view.updateFields(result!!)
            }
        } else {
            currentSchedule = Schedule()
        }
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        if (currentSchedule == null) {
            currentSchedule = Schedule()
        }
    }

    /**
     * Handles what happens when you click OK/Add in the Schedule dialog - Persists the schedule
     * to that clazz.
     */
    fun handleAddSchedule() {
        currentSchedule!!.scheduleClazzUid = currentClazzUid
        currentSchedule!!.scheduleActive = true

        //Creates ClazzLogs for today (since ClazzLogs are automatically only created for tomorrow)
        val runAfterInsertOrUpdate = Runnable {
            createClazzlogsForToday(UmAccountManager.getActivePersonUid(context), appDatabaseRepo, context)

            impl.scheduleChecks(context)
        }

        if (currentSchedule!!.scheduleUid == 0L) {
            GlobalScope.launch {
                scheduleDao.insertAsync(currentSchedule!!)
                runAfterInsertOrUpdate.run()
            }
        } else {
            GlobalScope.launch {
                scheduleDao.updateAsync(currentSchedule!!)
                val currentTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                appDatabaseRepo.clazzLogDao.cancelFutureInstances(
                        currentScheduleUid, currentTime, true)
                runAfterInsertOrUpdate.run()
            }
        }
    }

    /**
     * Cancels the schedule dialog
     */
    fun handleCancelSchedule() {
        currentSchedule = null
    }

    /**
     * Sets the picked "from" time from the dialog to the schedule object in the presenter. In ms
     * since the start of the day.
     *
     * @param time  The "from" time.
     */
    fun handleScheduleFromTimeSelected(time: Long) {
        currentSchedule!!.sceduleStartTime = time
    }

    /**
     * Sets the picked "to" time from the dialog to the schedule object in the presenter.
     *
     * @param time The "to" time
     */
    fun handleScheduleToTimeSelected(time: Long) {
        currentSchedule!!.scheduleEndTime = time
    }

    /**
     * Sets schedule from the position of drop down options
     * @param position  Position of drop down (spinner) selected
     * @param id        If of drop down (spinner) selected
     */
    fun handleScheduleSelected(position: Int, id: Long) {
        if (position == EVERY_DAY_SCHEDULE_POSITION) {
            currentSchedule!!.scheduleDay = -1
            view.hideDayPicker(true)
        } else {
            view.hideDayPicker(false)
        }
        currentSchedule!!.scheduleFrequency = position + 1

    }

    /**
     * Sets schedule Day on the currently editing schedule.
     * @param position  The position of the day according to the drop down options.
     */
    fun handleDaySelected(position: Int) {
        currentSchedule!!.scheduleDay = position + 1
    }

    fun createClazzlogsForToday(accountPersonUid: Long, dbRepo:UmAppDatabase, theContext: Any){


        //Note this calendar is created on the device's time zone.
        val startTime = UMCalendarUtil.getToday000000()

        val endTime = UMCalendarUtil.getToday235959()

        createClazzLogs(startTime, endTime,
                UmAccountManager.getActivePersonUid(theContext), dbRepo)
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

        var endT: Long = UMCalendarUtil.normalizeSecondsAndMillis(endTime)

        val startMsOfDay = (UMCalendarUtil.getHourOfDay24(startT) * 24 +
                UMCalendarUtil.getMinuteOfDay(startT) * 60 * 100).toLong()


        //Get a list of all classes the logged in user has access to:
        val clazzList = db.clazzDao.findAllClazzesWithSelectPermission(
                accountPersonUid)
        //Loop over the classes
        for (clazz in clazzList) {
            //Skipp classes that have no time zone
            //TODO: KMP TimeZone fix.
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

                var nextSchedule: Long = 0

                if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_DAILY) {

                    val tomorrowLong = UMCalendarUtil.getDateInMilliPlusDays(1)
                    val tomorrowDay = UMCalendarUtil.getDayOfWeek(tomorrowLong)
                    val today = UMCalendarUtil.getDayOfWeek(UMCalendarUtil.getDateInMilliPlusDays(0))

                    val dayOfWeek: Int
                    if (!incToday) {
                        dayOfWeek = tomorrowDay
                    } else {
                        dayOfWeek = today
                    }
                    //TODO: Associate with weekend feature in the future
                    if (dayOfWeek == 0) { //Sunday in DateTime klock
                        //skip
                        println("Today is a weekend. Skipping ClazzLog creation for today.")

                    } else if (db.scheduleDao.checkGivenDateAHolidayForClazz(startT,
                                    clazz.clazzUid)) {
                        //Its a holiday. Skip
                        println("Skipping holiday")

                    } else if (clazz.clazzEndTime != 0L && startT > clazz.clazzEndTime) {
                        //Date is ahead of clazz end date. Skipping.
                        println("Skipping cause current date is after Class's end date.")

                    } else if (clazz.clazzStartTime != 0L && startT < clazz.clazzStartTime) {
                        //Date is before Clazz's start date. Skipping
                        println("Skipping cause current date is before Class's start date.")
                    } else {

                        //This will get the next schedule for that day. For the same day, it will
                        //return itself if incToday is set to true, else it will go to next week.
                        nextSchedule = UMCalendarUtil.copyCalendarAndAdvanceTo(startT,
                                dayOfWeek, incToday)
                        nextSchedule = UMCalendarUtil.zeroOutTimeForGivenLongDate(nextSchedule)

                        //Now move it to desired hour:
                        nextSchedule = UMCalendarUtil.changeDatetoThis(nextSchedule, startTimeMins)

                    }

                } else if (schedule.scheduleFrequency == Schedule.SCHEDULE_FREQUENCY_WEEKLY) {

                    if (db.scheduleDao.checkGivenDateAHolidayForClazz(startT,
                                    clazz.clazzUid)) {
                        //Its a holiday. Skip it.
                        println("Skipping holiday")
                    } else if (clazz.clazzEndTime != 0L && startT > clazz.clazzEndTime) {
                        //Date is ahead of clazz end date. Skipping.
                        println("Skipping cause current date is after Class's end date.")

                    } else if (clazz.clazzStartTime != 0L && startT < clazz.clazzStartTime) {
                        //Date is before Clazz's start date. Skipping
                        println("Skipping cause current date is before Class's start date.")
                    } else {

                        //Will be true if today is schedule day
                        var today = UMCalendarUtil.getDayOfWeek(UMCalendarUtil.getToday000000())
                        today = today + 1
                        incToday = today == schedule.scheduleDay

                        //Get the day of next occurence.
                        nextSchedule = UMCalendarUtil.copyCalendarAndAdvanceTo(
                                startT, schedule.scheduleDay - 1, incToday)

                        //Set the day's timezone to Clazz
                        //TODO: TimeZone
//                        nextScheduleOccurence!!.setTimeZone(TimeZone.getTimeZone(timeZone))

                        nextSchedule = UMCalendarUtil.copyCalendarAndAdvanceTo(
                                startT, schedule.scheduleDay - 1, incToday)

                        //Set to 00:00
                        nextSchedule = UMCalendarUtil.zeroOutTimeForGivenLongDate(nextSchedule)

                        //Now move it to desired hour:
                        nextSchedule = UMCalendarUtil.changeDatetoThis(nextSchedule, startTimeMins)
                    }
                }

                if (nextSchedule != null && nextSchedule < endT) {
                    //this represents an instance of this class that should take place and
                    //according to the arguments provided, we should check that this instance exists
                    val logInstanceHash = ClazzLogDao.generateClazzLogUid(clazz.clazzUid,nextSchedule)
                    val existingLog = db.clazzLogDao.findByUid(logInstanceHash.toLong())

                    if (existingLog == null || existingLog!!.clazzLogCancelled) {
                        val newLog = ClazzLog(logInstanceHash.toLong(), clazz.clazzUid,
                                nextSchedule, schedule.scheduleUid)
                        db.clazzLogDao.replace(newLog)
                    }
                }
            }
        }
    }


    companion object{
        private fun findPendingLogsWithoutScheduledCheck(checkType: Int, scheduleCheckDao: ScheduledCheckDao): List<ClazzLog> {
            val todayZero = UMCalendarUtil.zeroOutTimeForGivenLongDate(DateTime.now().unixMillisLong)
            return scheduleCheckDao.findPendingLogsWithoutScheduledCheck(checkType, todayZero)
        }

        /**
         * ScheduledCheck runs locally, and is not sync'd. New ClazzLog objects can be created
         * by presenters, or the scheduler, but they might also arrive through the sync system (e.g.
         * for someone who just logged in).
         */
        fun createPendingScheduledChecks(scheduleCheckDao: ScheduledCheckDao) {

            // The checks are created for clazz logs (if not already created by this method earlier)

            //Get a list of all ClazzLogs that don't have a Schedule check of attendance reminder type.
            //TYPE_RECORD_ATTENDANCE_REMINDER
            val logsWithoutChecks = findPendingLogsWithoutScheduledCheck(
                    ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER, scheduleCheckDao)
            val newCheckList = ArrayList<ScheduledCheck>()

            for (clazzLog in logsWithoutChecks) {
                val recordReminderCheck = ScheduledCheck(
                        clazzLog.logDate,
                        ScheduledCheck.TYPE_RECORD_ATTENDANCE_REMINDER,
                        ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                                clazzLog.clazzLogUid)
                recordReminderCheck.scClazzLogUid = clazzLog.clazzLogUid
                newCheckList.add(recordReminderCheck)
            }

            //Create Scheduled Checks for repetition checks for TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER
            val logsWithoutRepetitionChecks = findPendingLogsWithoutScheduledCheck(
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER, scheduleCheckDao)

            for (clazzLog in logsWithoutRepetitionChecks) {
                val repetitionReminderOfficerCheck = ScheduledCheck(
                        clazzLog.logDate,
                        ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                        ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                                clazzLog.clazzLogClazzUid
                )
                repetitionReminderOfficerCheck.scClazzLogUid = clazzLog.clazzLogUid
                newCheckList.add(repetitionReminderOfficerCheck)
            }

            //Create Scheduled Checks for TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH
            val logsWithoutAbsentHighChecks = findPendingLogsWithoutScheduledCheck(
                    ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_TIME_HIGH, scheduleCheckDao)

            for (clazzLog in logsWithoutAbsentHighChecks) {
                val absentHighCheck = ScheduledCheck(
                        clazzLog.logDate,
                        ScheduledCheck.TYPE_CHECK_ABSENT_REPETITION_LOW_OFFICER,
                        ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                                clazzLog.clazzLogClazzUid
                )
                absentHighCheck.scClazzLogUid = clazzLog.clazzLogUid
                newCheckList.add(absentHighCheck)
            }

            //Create Scheduled Checks for TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH
            val logsWithoutClazzAttendanceHigh = findPendingLogsWithoutScheduledCheck(
                    ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH, scheduleCheckDao)
            for (clazzLog in logsWithoutClazzAttendanceHigh) {
                val clazzAttendanceHighSC = ScheduledCheck(
                        clazzLog.logDate,
                        ScheduledCheck.TYPE_CHECK_CLAZZ_ATTENDANCE_BELOW_THRESHOLD_HIGH,
                        ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                                clazzLog.clazzLogClazzUid
                )
                clazzAttendanceHighSC.scClazzLogUid = clazzLog.clazzLogUid
                newCheckList.add(clazzAttendanceHighSC)
            }

            //Next day attendance taken or not checks. - Teachers, Officers, Admins get this
            val logsWithoutNextDayCheck = findPendingLogsWithoutScheduledCheck(
                    ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER, scheduleCheckDao)

            for (clazzLog in logsWithoutNextDayCheck) {
                val clazzLogDateMilli = clazzLog.logDate
                val clazzLogUid = clazzLog.clazzLogUid

                //OR: We just Add one day from Clazzlog logdate

                val clazzLogDate = DateTime(clazzLogDateMilli)
                if (clazzLogDate != null) {
                    println("valid clazzlog")
                }
                //No need for timezone . It will be device midnight time.
                val tomorrowSameTime = UMCalendarUtil.getDateInMilliPlusDaysRelativeTo(clazzLogDateMilli, 1)
                val tomorrowZeroHour = UMCalendarUtil.zeroOutTimeForGivenLongDate(tomorrowSameTime)

                val nextDayCheck = ScheduledCheck(
                        tomorrowZeroHour,
                        ScheduledCheck.TYPE_CHECK_ATTENDANCE_NOT_RECORDED_DAY_AFTER,
                        ScheduledCheck.PARAM_CLAZZ_LOG_UID + "=" +
                                clazzLogUid)
                nextDayCheck.scClazzLogUid = clazzLogUid
                newCheckList.add(nextDayCheck)

            }

            //Insert All new Scheduled Checks
            scheduleCheckDao.insertList(newCheckList)
        }

    }
}
