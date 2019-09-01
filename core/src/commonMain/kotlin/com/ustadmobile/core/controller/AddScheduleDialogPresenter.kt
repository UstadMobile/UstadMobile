package com.ustadmobile.core.controller


import com.soywiz.klock.DateTime
import com.ustadmobile.core.db.UmAppDatabase
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

    internal var currentClazzUid: Long = -1
    private var currentScheduleUid = -1L

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


    override fun onCreate(savedState: Map<String, String?>?) {
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
            scheduleDao.createClazzLogsForToday(
                    UmAccountManager.getActivePersonUid(context), appDatabaseRepo)
            //If you want it to create ClazzLogs for every day of schedule (useful for testing):
            //scheduleDao.createClazzLogsForEveryDayFromDays(5,
            //        UmAccountManager.getActivePersonUid(getContext()), appDatabaseRepo);

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



    companion object{
        fun findPendingLogsWithoutScheduledCheck(checkType: Int, scheduleCheckDao: ScheduledCheckDao): List<ClazzLog> {
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
