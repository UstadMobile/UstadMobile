package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMCalendarUtil
import com.ustadmobile.core.view.ClazzActivityEditView
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.ARG_CLAZZACTIVITY_LOGDATE
import com.ustadmobile.core.view.ClazzActivityEditView.Companion.ARG_CLAZZACTIVITY_UID
import com.ustadmobile.core.view.ClazzActivityListView
import com.ustadmobile.core.view.ClazzListView.Companion.ARG_CLAZZ_UID
import com.ustadmobile.lib.db.entities.ClazzActivityWithChangeTitle
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The ClazzActivityList Presenter - Responsible for the logic of Activity Tab in Clazz Detail.
 * ie: Showing Activity List on the view and showing Activity bar charts as well.
 *
 */
class ClazzActivityListPresenter(context: Any, arguments: Map<String, String>?,
                                 view: ClazzActivityListView,
                                 val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance)
    : UstadBaseController<ClazzActivityListView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var currentClazzUid: Long = -1
    private var clazzActivityChangeUid: Long = -1

    private var changeToIdMap: HashMap<Float, Long>? = null
    private var barMapWithOGDateTimes: HashMap<Float, Long>? = null

    //Provider
    private var providerList: DataSource.Factory<Int, ClazzActivityWithChangeTitle>? = null

    var isCanEdit: Boolean = false
    private var loggedInPersonUid = 0L

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    private val clazzActivityDao = repository.clazzActivityDao
    private val activityChangeDao = repository.clazzActivityChangeDao


    init {

        //Get Clazz Uid and save it.
        if (arguments!!.containsKey(ARG_CLAZZ_UID)) {
            currentClazzUid = arguments!!.get(ARG_CLAZZ_UID)!!.toLong()
        }

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

    }

    /**
     * Method to update the Activity Change options in the view for the bar chart.
     */
    private fun updateChangeOptions() {

        GlobalScope.launch {
            val result = activityChangeDao.findAllClazzActivityChangesAsync()
            changeToIdMap = HashMap()
            val presetAL = ArrayList<String>()
            var i = 0f
            for (everyChange in result!!) {
                presetAL.add(everyChange.clazzActivityChangeTitle!!)

                // Save mapping of position and activity change's uid to handle it.
                changeToIdMap!![i] = everyChange.clazzActivityChangeUid
                i++
            }
            val objectArr = presetAL.toTypedArray()
            val strArr = arrayOfNulls<String?>(objectArr.size)
            for (j in objectArr.indices) {
                strArr[j] = objectArr[j]
            }
            view.runOnUiThread(Runnable { view.setClazzActivityChangesDropdownPresets(strArr) })
        }
    }

    /**
     * Order:
     * 1. Get all the activity for the current clazz.
     * 2. Set the provider to the view
     * 3. Update Activity Change presets from Database
     * 4. Populate chart.
     *
     * @param savedState    The savedState
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        //Populate the provider
        providerList = repository.clazzActivityDao.findWithChangeTitleByClazzUid(currentClazzUid)

        setProviderOnView()

        //Update Change options
        updateChangeOptions()

        //Permissions update
        checkPermissions()

    }

    fun checkPermissions() {
        val clazzDao = repository.clazzDao
        GlobalScope.launch {
            val result = clazzDao.personHasPermissionWithClazz(loggedInPersonUid, currentClazzUid,
                    Role.PERMISSION_CLAZZ_LOG_ACTIVITY_INSERT)
            isCanEdit = result
            view.setFABVisibility(result)
        }
    }

    private fun setProviderOnView() {
        //set Provider.
        view.setListProvider(providerList!!)
    }

    /**
     * Sets the current Activity Change Uid for the charts.
     * @param clazzActivityChangeUid The Activity Change Uid to be set to this presenter
     * (usually for charts)
     */
    fun setClazzActivityChangeUid(clazzActivityChangeUid: Long) {
        if (changeToIdMap != null) {
            if (changeToIdMap!!.containsKey(clazzActivityChangeUid.toFloat())) {
                this.clazzActivityChangeUid = changeToIdMap!![clazzActivityChangeUid.toFloat()]!!
            }
        }
    }

    /**
     * Handles going to Creating a new Activity. This is to be called when "Record Activity" FAB is
     * clicked.
     */
    fun goToNewClazzActivityEditActivity(flag: Int) {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())
        args.put(ARG_CLAZZACTIVITY_LOGDATE, UMCalendarUtil.getDateInMilliPlusDays(0).toString())

        impl.go(ClazzActivityEditView.VIEW_NAME, args, context, flag)

    }

    fun goToNewClazzActivityEditActivity(clazzActivityUid: Long, flag: Int) {
        val args = HashMap<String, String>()
        args.put(ARG_CLAZZACTIVITY_UID, clazzActivityUid.toString())
        args.put(ARG_CLAZZ_UID, currentClazzUid.toString())

        impl.go(ClazzActivityEditView.VIEW_NAME, args, context, flag)

    }

    /**
     * The common method to update the Activity Bar chart based on the duration given to it.
     * The Activity Change is already set from this Presenter's setters.
     * The method queries the database and gets a daily aggregate for that clazz based on the
     * duration given to it (WEEK/MONTH/YEAR).
     *
     * @param duration The duration constant that is defined in ClazzLogListView for WEEK, MONTH,
     * YEAR.
     */
    fun getActivityDataAndUpdateCharts(duration: Int) {

        //Reset the report buttons
        view.resetReportButtons()

        val barDataMap = LinkedHashMap<Float, Float>()
        val toDate = UMCalendarUtil.getDateInMilliPlusDays(0)
        var fromDate: Long? = toDate
        var groupOffset = 1

        when (duration) {
            CHART_DURATION_LAST_WEEK -> fromDate = UMCalendarUtil.getDateInMilliPlusDays(-7)
            CHART_DURATION_LAST_MONTH -> {
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-31)
                groupOffset = 7
            }
            CHART_DURATION_LAST_YEAR -> {
                fromDate = UMCalendarUtil.getDateInMilliPlusDays(-365)
                groupOffset = 30
            }
            else -> {
            }
        }//Do nothing.

        //Get aggregate daily data about Clazz Activity.
        val finalGroupOffset = groupOffset
        GlobalScope.launch {
            val result = clazzActivityDao.getDailyAggregateFeedbackByActivityChange(currentClazzUid,
                    fromDate!!, toDate, clazzActivityChangeUid)

            val barDataMapGrouped = LinkedHashMap<Float, Float>()

            barMapWithOGDateTimes = HashMap()
            var f = 0f
            var h = 1f
            var g = 0f

            var goodGrouped = 0
            var badGrouped = 0

            for (everyDayAttendance in result!!) {

                f++
                //h++;

                val good = everyDayAttendance.good
                val bad = everyDayAttendance.bad
                val thisDate = everyDayAttendance.dayDate

                barMapWithOGDateTimes!![f] = thisDate

                //Sum up the good and bad for the day
                if (good > bad) {
                    barDataMap[f] = good.toFloat()
                } else {
                    barDataMap[f] = -bad.toFloat()
                }

                if (h <= finalGroupOffset) {
                    goodGrouped = goodGrouped + good
                    badGrouped = badGrouped + bad

                    h++
                    if (h > finalGroupOffset) {
                        g++

                        if (goodGrouped > badGrouped) {
                            barDataMapGrouped[g] = goodGrouped.toFloat()
                        } else {
                            barDataMapGrouped[g] = (-badGrouped).toFloat()
                        }

                        h = 1f
                        goodGrouped = 0
                        badGrouped = 0
                    }
                }
            }

            view.updateActivityBarChart(barDataMapGrouped)
        }
    }

    companion object {
        //Attendance chart time duration flags.
        val CHART_DURATION_LAST_WEEK = 1
        val CHART_DURATION_LAST_MONTH = 2
        val CHART_DURATION_LAST_YEAR = 3
    }
}
