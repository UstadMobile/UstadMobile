package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.schedule.localEndOfDay
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.attendancePercentage
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.latePercentage
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_NEXT
import com.ustadmobile.door.*
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzWithSchool
import com.ustadmobile.lib.db.entities.Role
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

    enum class RecordAttendanceOption(val commandId: Int,val messageId: Int ) {
        RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE(1, MessageID.record_attendance_for_most_recent_occurrence),
        RECORD_ATTENDANCE_NEW_SCHEDULE(2, MessageID.add_a_new_occurrence)
    }

    class ClazzLogListSortOption(
        val sortOrder: SortOrder,
        context: Any,
        di: DI
    ) : MessageIdOption(sortOrder.messageId, context, di = di)

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        clazzUidFilter = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0
        updateListOnView()
        view.sortOptions = SortOrder.values().toList().map { ClazzLogListSortOption(it, context, di) }
        view.graphData = graphDisplayData

        presenterScope.launch {
            val hasAttendancePermission = repo.clazzDao.personHasPermissionWithClazz(
                    accountManager.activeAccount.personUid, clazzUidFilter,
                    Role.PERMISSION_CLAZZ_LOG_ATTENDANCE_INSERT)

            if(!hasAttendancePermission) {
                view.recordAttendanceOptions = listOf()
                return@launch
            }

            repo.clazzLogDao.clazzHasScheduleLive(clazzUidFilter, ClazzLog.STATUS_INACTIVE)
                    .observeWithLifecycleOwner(lifecycleOwner) { hasClazzLogs ->
                        view.recordAttendanceOptions = if(hasClazzLogs == true) {
                            listOf(RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE,
                                    RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE)
                        }else {
                            listOf(RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE)
                        }
                    }

        }
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        //This should actually return a value to determine whether or not the user can record / view attendance.
        //Here the fab does not directly create a new item, rather it opens the most recent log
        return true
    }

    private fun updateListOnView() {
        presenterScope.launch {
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
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                ClazzLogEditAttendanceView.VIEW_NAME,
                ClazzLog::class,
                ClazzLog.serializer(),
                arguments = mutableMapOf(
                    UstadView.ARG_ENTITY_UID to entry.clazzLogUid.toString())
            )
        )
    }

    fun handleClickRecordAttendance(option: RecordAttendanceOption) {
        if(option == RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE) {
            GlobalScope.launch(doorMainDispatcher()) {
                val lastLog = db.clazzLogDao.findByClazzUidWithinTimeRangeAsync(
                        clazzUidFilter, 0, Long.MAX_VALUE,
                        ClazzLog.STATUS_INACTIVE, 1).firstOrNull()
                if(lastLog != null){
                    handleClickEntry(lastLog)
                }
            }
        }else {
            val newClazzLog = ClazzLog().also {
                it.clazzLogClazzUid = clazzUidFilter
                it.logDate = systemTimeInMillis()
            }

            navigateForResult(
                NavigateForResultOptions(
                    this, newClazzLog,
                    ClazzLogEditView.VIEW_NAME,
                    ClazzLog::class,
                    ClazzLog.serializer(),
                    arguments = mutableMapOf(ARG_NEXT to ClazzLogEditAttendanceView.VIEW_NAME)
                )
            )
        }
    }

    override fun handleClickCreateNewFab() {
        GlobalScope.launch(doorMainDispatcher()) {
            val lastLog = db.clazzLogDao.findByClazzUidWithinTimeRangeAsync(
                    clazzUidFilter, 0, Long.MAX_VALUE,
                    ClazzLog.STATUS_INACTIVE, 1).firstOrNull()
            if(lastLog != null){
                handleClickEntry(lastLog)
            }
        }
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {}

    fun handleClickGraphDuration(days: Int) {
        val endOfDay = DateTime.now().toOffsetByTimezone(clazzTimeZone ?: "UTC")
                .localEndOfDay.utc.unixMillisLong
        graphDateRange = (endOfDay - days.days.millisecondsLong) to endOfDay
        graphDbData?.removeObserver(graphObserver)
        graphDbData = repo.clazzLogDao.findByClazzUidWithinTimeRangeLive(clazzUidFilter,
                graphDateRange.first, graphDateRange.second, ClazzLog.STATUS_RECORDED)
        graphDbData?.observe(lifecycleOwner, graphObserver)
    }

    override fun handleClickSortOrder(sortOption: IdOption) {
        val sortOrder = (sortOption as? ClazzLogListSortOption)?.sortOrder ?: return
        if(sortOrder != currentSortOrder) {
            currentSortOrder = sortOrder
            updateListOnView()
        }
    }
}