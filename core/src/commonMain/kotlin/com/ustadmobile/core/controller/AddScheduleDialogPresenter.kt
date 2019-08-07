package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ScheduleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.AddScheduleDialogView
import com.ustadmobile.core.view.AddScheduleDialogView.Companion.EVERY_DAY_SCHEDULE_POSITION
import com.ustadmobile.core.view.ClazzEditView.Companion.ARG_SCHEDULE_UID
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.Schedule
import kotlinx.coroutines.Runnable


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

    private val appDatabaseRepo: UmAppDatabase

    internal var currentClazzUid: Long = -1
    private var currentScheduleUid = -1L

    init {

        appDatabaseRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        scheduleDao = appDatabaseRepo.scheduleDao

        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        if (arguments!!.containsKey(ARG_SCHEDULE_UID)) {
            currentScheduleUid = arguments!!.get(ARG_SCHEDULE_UID)!!.toLong()
        }

        if (currentScheduleUid > 0) {
            scheduleDao.findByUidAsync(currentScheduleUid, object : UmCallback<Schedule> {
                override fun onSuccess(result: Schedule?) {
                    currentSchedule = result
                    view.updateFields(result!!)
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            currentSchedule = Schedule()
        }
    }


    fun onCreate(savedState: Map<String, String>?) {
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
        currentSchedule!!.isScheduleActive = true

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
            scheduleDao.insertAsync(currentSchedule!!, object : UmCallback<Long> {
                override fun onSuccess(result: Long?) {
                    runAfterInsertOrUpdate.run()
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
        } else {
            scheduleDao.updateAsync(currentSchedule!!, object : UmCallback<Int> {
                override fun onSuccess(result: Int?) {

                    val currentTime = UMCalendarUtil.getDateInMilliPlusDays(0)
                    appDatabaseRepo.clazzLogDao.cancelFutureInstances(
                            currentScheduleUid, currentTime, true)
                    runAfterInsertOrUpdate.run()
                }

                override fun onFailure(exception: Throwable?) {
                    print(exception!!.message)
                }
            })
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

}
