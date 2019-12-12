package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClassLogDetailView
import com.ustadmobile.core.view.ClassLogListView
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_MONTH
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_WEEK
import com.ustadmobile.core.view.ClassLogListView.Companion.CHART_DURATION_LAST_YEAR
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord.Companion.STATUS_ATTENDED
import com.ustadmobile.lib.db.entities.ClazzLogWithScheduleStartEndTimes
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * The Presenter/Controller for ClazzLogListFragment. This is responsible for the logic behind
 * displaying every Clazz Attendance Logs via the database provider from the database/repository.
 * It is also responsible for the logic for displaying attendance charts for that class.
 */
class ClazzLogListPresenter(context: Any, arguments: Map<String, String>?, view: ClassLogListView,
                            val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClassLogListView>(context, arguments!!, view) {

    private var currentClazzUid = 0L

    private var clazzLogListProvider: DataSource.Factory<Int, ClazzLogWithScheduleStartEndTimes>? =
            null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val loggedInPersonUid: Long?

    init {

        //Get clazz uid and set it
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        //Permissions
        checkPermissions()
    }

    /**
     * Check permission and update the view accordingly
     */
    fun checkPermissions() {
        val clazzDao = repository.clazzDao
        GlobalScope.launch {
            var result = clazzDao.personHasPermission(loggedInPersonUid!!, currentClazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT)
            if(result == null){
                result = false
            }
            view.setFABVisibility(result)
        }
    }

    /**
     * In Order:
     * 1. This populates the Attendance/Log Entry list provider and sets it to the View.
     * 2. Generate attendance line chart
     * 3. Generate attendance bar chart
     *
     * This will be called when the implementation's View is ready.
     * (ie: on Android, this is called in the Fragment's onCreateView() )
     *
     * @param savedState    The savedState
     */
    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        GlobalScope.launch {
            clazzLogListProvider =
                    repository.clazzLogDao.findByClazzUidNotCancelledWithSchedule(currentClazzUid)
            setProviderToView()
        }

    }

    /**
     * Sets the clazz log list / clazz attendance list UMProvider provider set in this Presenter
     * to the View object.
     */
    private fun setProviderToView() {
        view.setClazzLogListProvider(clazzLogListProvider!!)
    }

    /**
     * Method logic to go to the Log Detail activity - To see the attendance entry details and edit
     * them.
     *
     * @param clazzLog  The clazz log entry uid that we want to edit.
     */
    fun goToClazzLogDetailActivity(clazzLog: ClazzLog) {

        val args = HashMap<String, String>()
        args.put(ClassLogDetailView.ARG_CLAZZ_LOG_UID, clazzLog.clazzLogUid.toString())
        impl.go(ClassLogDetailView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * Method logic that goes to ClazzLogDetail view (take attendance) for the class we're in.
     */
    fun goToNewClazzLogDetailActivity() {


        GlobalScope.launch {
            val result = repository.clazzLogDao.findMostRecentByClazzUid(currentClazzUid)

            if (result == null) {
                //Check if no schedule
                val clazzSchedules =
                        repository.scheduleDao.findAllSchedulesByClazzUidAsync(currentClazzUid)
                if(clazzSchedules.isEmpty()) {
                    view.showMessage(MessageID.no_schedule_message)
                }else{
                    //TODO
                    //Find next scheduled attendance
//                    val today = UMCalendarUtil.getDayOfWeek(UMCalendarUtil.getDateInMilliPlusDays(0)) + 1
//                    for(s in clazzSchedules){
//                        val sday = s.scheduleDay
//                        if(sday != 0){ //Weekly
//
//                            //TODO: Get number of days
//                            val days = 42
//                            val message = impl.getString(MessageID.next_attendance_message, view.viewContext) +
//                                    " " + impl.getString(MessageID.it_will_start_in, view.viewContext) + " "  +
//                                    days + " " + impl.getString(MessageID.days, view.viewContext)
//                            view.showMessage(message)
//
//
//                        }else{
//
//                            val message = impl.getString(MessageID.next_attendance_message, view.viewContext) +
//                                    " " + impl.getString(MessageID.it_will_start_tomorrow, view.viewContext) + ""
//                            view.showMessage(message)
//                        }
//                    }

                    view.showMessage(MessageID.next_attendance_message)

                }
            } else {
                val args = HashMap<String, String>()
                args.put(ClassLogDetailView.ARG_MOST_RECENT_BY_CLAZZ_UID,
                        currentClazzUid.toString())
                impl.go(ClassLogDetailView.VIEW_NAME, args, view.viewContext)
            }
        }

    }

    /**
     * Method that takes the duration flag and calculated daily attendance numbers for the current
     * class and updates both the line and bar charts.
     *
     * @param duration The duration flag (CHART_DURATION_LAST_WEEK, CHART_DURATION_LAST_MONTH,
     * CHART_DURATION_LAST_YEAR) as per defined in ClassLogListView
     */
    fun getAttendanceDataAndUpdateCharts(duration: Int) {

        //Reset report buttons
        view.resetReportButtons()

        val lineDataMap = LinkedHashMap<Float, Float>()
        val barDataMap = LinkedHashMap<Float, Float>()
        val toDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        var fromDate = toDate

        when (duration) {
            CHART_DURATION_LAST_WEEK -> {
                //7
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7)
                for (i in -7 until -1) {
                    lineDataMap[UMCalendarUtil.getDateInMilliPlusDays(i).toFloat() / 1000] = -1f
                }
            }
            CHART_DURATION_LAST_MONTH -> {
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31)
                for (i in -31 until -1) {
                    lineDataMap[UMCalendarUtil.getDateInMilliPlusDays(i).toFloat() / 1000] = -1f
                }
            }
            CHART_DURATION_LAST_YEAR -> {
                //31
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365)
                for (i in -365 until -1) {
                    lineDataMap[UMCalendarUtil.getDateInMilliPlusDays(i).toFloat() / 1000] = -1f
                }
            }
            else -> {
            }
        }//Do nothing.

        //Calculate daily attendance numbers from the database for the line chart.
        val attendanceRecordDao = repository.clazzLogAttendanceRecordDao
        GlobalScope.launch {
            val result =
                    attendanceRecordDao.findDailyAttendanceByClazzUidAndDateAsync(currentClazzUid,
                            fromDate, toDate)

            //TODOne: KMP Manipulate this on UMCalendar side and fix this.
            for (everyDayAttendance in result!!) {
                //null time so that we're only working wiht the same day's group by
                val dd = everyDayAttendance.logDate
                val d = UMCalendarUtil.zeroOutTimeForGivenLongDate(dd)
                val a = everyDayAttendance.attendancePercentage
                lineDataMap[d.toFloat() / 1000] = a

            }

            //Remove messy date keys
            val ldpi = lineDataMap.entries.iterator()
            val lineDataMapFixedX = LinkedHashMap<Float, Float>()
            var l = 0f
            while (ldpi.hasNext()) {
                l++
                lineDataMapFixedX[l] = ldpi.next().value
            }

            view.updateAttendanceLineChart(lineDataMapFixedX)

        }

        //Calculate attendance average numbers for the bar chart.
        val clazzMemberDao = repository.clazzMemberDao

        GlobalScope.launch {
            val result =
                    clazzMemberDao.findAttendanceSpreadByThresholdForTimePeriodAndClazzAndType(
                            STATUS_ATTENDED, currentClazzUid, fromDate, toDate)
            barDataMap[3f] = result!!.high / 100
            barDataMap[2f] = result.mid / 100
            barDataMap[1f] = result.low / 100
            view.updateAttendanceBarChart(barDataMap)
        }

    }
}
