package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.attendancePercentage
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.latePercentage
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.*
import com.ustadmobile.door.*
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzWithSchool
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI

class ClazzLogListAttendancePresenter(context: Any, arguments: Map<String, String>, view: ClazzLogListAttendanceView,
                          di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzLogListAttendanceView, ClazzLog>(context, arguments, view, di, lifecycleOwner) {

    //List of points to plot.
    data class AttendanceGraphData(val percentageAttendedSeries: List<Pair<Long, Float>>,
                                   val percentageLateSeries: List<Pair<Long, Float>>,
                                   val graphDateRange: Pair<Long, Long>)

    var currentSortOrder: SortOrder = SortOrder.ORDER_NAME_ASC

    var clazzUidFilter: Long = 0

    private var clazzWithSchool: ClazzWithSchool? = null

    private var graphDbData: DoorLiveData<List<ClazzLog>>? = null

    private var graphDisplayData = DoorMutableLiveData<AttendanceGraphData>()

    private var graphDateRange = Pair(0L, 0L)

    private var clazzTimeZone: String? = null

    //It is required to explicitly specify the type due to a compiler bug:
    // https://youtrack.jetbrains.com/issue/KT-20996
    private val graphObserver : DoorObserver<List<ClazzLog>> = object: DoorObserver<List<ClazzLog>> {
        override fun onChanged(t: List<ClazzLog>) {
            graphDisplayData.sendValue(AttendanceGraphData(
                t.map { it.logDate to it.attendancePercentage() },
                t.map { it.logDate to it.latePercentage() },
                graphDateRange
            ))
        }
    }

    enum class SortOrder(val messageId: Int) {
        ORDER_NAME_ASC(MessageID.sort_by_name_asc),
        ORDER_NAME_DSC(MessageID.sort_by_name_desc)
    }

    class ClazzLogListSortOption(val sortOrder: SortOrder, context: Any) : MessageIdOption(sortOrder.messageId, context)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUidFilter = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzLogListSortOption(it, context) }
        view.graphData = graphDisplayData
        repo.clazzLogDao.clazzHasScheduleLive(clazzUidFilter, ClazzLog.STATUS_INACTIVE)
                .observeWithLifecycleOwner(lifecycleOwner) { hasClazzLogs ->
                    view.recordAttendanceButtonVisible = hasClazzLogs ?: false
                }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //This should actually return a value to determine whether or not the user can record / view attendance.
        //Here the fab does not directly create a new item, rather it opens the most recent log
        return true
    }

    private fun updateListOnView() {
        GlobalScope.launch {
            clazzWithSchool = repo.clazzDao.getClazzWithSchool(clazzUidFilter)
            withContext((doorMainDispatcher())) {
                clazzTimeZone = clazzWithSchool?.effectiveTimeZone() ?: "UTC"
                if(view.clazzTimeZone == null) {
                    handleClickGraphDuration(7)
                }

                view.clazzTimeZone = clazzTimeZone
                view.list = repo.clazzLogDao.findByClazzUidAsFactory(clazzUidFilter,
                        ClazzLog.STATUS_RESCHEDULED)
            }
        }

    }

    override fun handleClickEntry(entry: ClazzLog) {
        systemImpl.go(ClazzLogEditAttendanceView.VIEW_NAME,
            mapOf(UstadView.ARG_ENTITY_UID to entry.clazzLogUid.toString()), context)
    }

    override fun handleClickCreateNewFab() {
        GlobalScope.launch(doorMainDispatcher()) {
            val lastLog = db.clazzLogDao.findByClazzUidWithinTimeRange(
                    clazzUidFilter, 0, Long.MAX_VALUE,
                    ClazzLog.STATUS_INACTIVE, 1).firstOrNull()
            if(lastLog != null){
                handleClickEntry(lastLog)
            }
        }
    }

    fun handleClickGraphDuration(days: Int) {
        val endOfDay = DateTime.now().toOffsetByTimezone(clazzTimeZone ?: "UTC")
                .localEndOfDay.utc.unixMillisLong
        graphDateRange = (endOfDay - days.days.millisecondsLong) to endOfDay
        graphDbData?.removeObserver(graphObserver)
        graphDbData = repo.clazzLogDao.findByClazzUidWithinTimeRangeLive(clazzUidFilter,
                graphDateRange.first, graphDateRange.second, ClazzLog.STATUS_RECORDED)
        graphDbData?.observe(lifecycleOwner, graphObserver)
    }

    override fun handleClickSortOrder(sortOption: MessageIdOption) {
        val sortOrder = (sortOption as? ClazzLogListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}