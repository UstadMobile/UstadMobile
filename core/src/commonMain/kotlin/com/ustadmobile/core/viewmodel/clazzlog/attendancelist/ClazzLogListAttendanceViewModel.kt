package com.ustadmobile.core.viewmodel.clazzlog.attendancelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.impl.appstate.FabUiState
import com.ustadmobile.core.impl.appstate.LoadingUiState
import com.ustadmobile.core.impl.nav.UstadSavedStateHandle
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.ext.whenSubscribed
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.ListPagingSourceFactory
import com.ustadmobile.core.viewmodel.UstadListViewModel
import com.ustadmobile.core.viewmodel.clazz.detail.ClazzDetailViewModel
import com.ustadmobile.core.viewmodel.clazzlog.edit.ClazzLogEditViewModel
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.core.viewmodel.person.list.EmptyPagingSource
import com.ustadmobile.lib.db.entities.ClazzLog
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import org.kodein.di.DI

data class ClazzLogListAttendanceUiState(

    /**
     * The data that will be used to draw the chart. This is a list with pairs that represent the
     * x/y coordinates. The x is the timestamp, the y is the attendance (between 0 and 100)
     */
    val graphData: AttendanceGraphData? = null,

    /**
     * The course time zone (used to format all timestamps)
     */
    val timeZoneId: String = TimeZone.currentSystemDefault().id,

    val recordAttendanceOptions: List<ClazzLogListAttendanceViewModel.RecordAttendanceOption> =
        emptyList(),

    val clazzLogsList: ListPagingSourceFactory<ClazzLog> = { EmptyPagingSource() },

    val fieldsEnabled: Boolean = true,

    val selectedChipId: Int = 7,

    val viewIdToNumDays: List<MessageIdOption2> = listOf(
        MessageIdOption2(MR.strings.last_week, 7),
        MessageIdOption2(MR.strings.last_month, 30),
        MessageIdOption2(MR.strings.last_three_months, 90)
    ),

    val createNewOptionsVisible: Boolean = false,
)

data class AttendanceGraphData(

    val percentageAttendedSeries: List<Pair<Long, Float>>,

    val percentageLateSeries: List<Pair<Long, Float>>,

    val graphDateRange: Pair<Long, Long>
)

class ClazzLogListAttendanceViewModel(
    di: DI,
    savedStateHandle: UstadSavedStateHandle,
): UstadListViewModel<ClazzLogListAttendanceUiState>(
    di, savedStateHandle, ClazzLogListAttendanceUiState(), ClazzDetailViewModel.DEST_NAME,
) {

    enum class RecordAttendanceOption(val stringResource: StringResource ) {
        RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE(MR.strings.record_attendance_for_most_recent_occurrence),
        RECORD_ATTENDANCE_NEW_SCHEDULE(MR.strings.add_a_new_occurrence);
    }

    //List of points to plot.
    @Suppress("unused") //For graph data to come
    data class AttendanceGraphData(
        val percentageAttendedSeries: List<Pair<Long, Float>>,
        val percentageLateSeries: List<Pair<Long, Float>>,
        val graphDateRange: Pair<Long, Long>,
    )


    private val clazzUid = savedStateHandle[UstadView.ARG_CLAZZUID]?.toLong()
        ?: throw IllegalArgumentException("No clazzUid specified")

    private val pagingSourceFactory: ListPagingSourceFactory<ClazzLog> = {
        activeRepoWithFallback.clazzLogDao().findByClazzUidAsFactory(
            clazzUid = clazzUid,
            excludeStatus = ClazzLog.STATUS_RESCHEDULED,
        )
    }

    private data class PermissionAndExistingLogs(
        val hasRecordPermission: Boolean,
        val hasExistingLogs: Boolean,
    )

    init {
        _uiState.update { prev ->
            prev.copy(clazzLogsList = pagingSourceFactory)
        }

        viewModelScope.launch {
            _uiState.whenSubscribed {
                launch {
                    activeRepoWithFallback.clazzDao().findByUidAsFlow(clazzUid).collect {clazz ->
                        _uiState.takeIf {
                            it.value.timeZoneId != clazz?.clazzTimeZone
                        }?.update { prev ->
                            prev.copy(
                                timeZoneId = clazz?.clazzTimeZone ?: "UTC"
                            )
                        }

                        _appUiState.takeIf { it.value.title != clazz?.clazzName }?.update { prev ->
                            prev.copy(
                                title = clazz?.clazzName
                            )
                        }
                    }
                }

                launch {
                    val hasPermissionFlow = activeRepoWithFallback.coursePermissionDao().personHasPermissionWithClazzAsFlow2(
                        accountPersonUid = activeUserPersonUid,
                        clazzUid = clazzUid,
                        permission = PermissionFlags.COURSE_ATTENDANCE_RECORD,
                    )

                    val hasExistingLogs = activeRepoWithFallback.clazzLogDao().clazzHasScheduleLive(
                        clazzUid = clazzUid,
                        excludeStatusFilter = ClazzLog.STATUS_RESCHEDULED,
                    )

                    hasPermissionFlow.combine(hasExistingLogs) { hasPermission, hasLogs ->
                        PermissionAndExistingLogs(hasPermission, hasLogs)
                    }.collect {
                        val options = buildList {
                            if(it.hasRecordPermission)
                                add(RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE)
                            if(it.hasRecordPermission && it.hasExistingLogs)
                                add(RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE)
                        }

                        _uiState.update { prev ->
                            prev.copy(
                                recordAttendanceOptions = options,
                            )
                        }
                        _appUiState.update { prev ->
                            prev.copy(
                                fabState = FabUiState(
                                    visible = options.isNotEmpty(),
                                    text = systemImpl.getString(MR.strings.record_attendance),
                                    icon = FabUiState.FabIcon.ADD,
                                    onClick = {
                                        if(options.size > 1) {
                                            _uiState.update { prev ->
                                                prev.copy(createNewOptionsVisible = true)
                                            }
                                        }else {
                                            onClickRecordAttendance(
                                                RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE
                                            )
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun onDismissCreateNewOptions() {
        _uiState.update { prev ->
            prev.copy(
                createNewOptionsVisible = false
            )
        }
    }

    override fun onUpdateSearchResult(searchText: String) {
        //Do nothing - we are not handling search results here.
    }

    override fun onClickAdd() {

    }

    fun onClickRecordAttendance(option: RecordAttendanceOption) {
        when(option) {
            RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE -> {
                navigateToCreateNew(
                    editViewName = ClazzLogEditViewModel.DEST_NAME,
                    extraArgs = mapOf(
                        ARG_TIME_ZONE to _uiState.value.timeZoneId,
                        UstadView.ARG_CLAZZUID to clazzUid.toString(),
                    )
                )
                onDismissCreateNewOptions()
            }
            RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE -> {
                if(loadingState == LoadingUiState.INDETERMINATE)
                    return

                loadingState = LoadingUiState.INDETERMINATE

                viewModelScope.launch {
                    val mostRecentLogUid = activeRepoWithFallback.clazzLogDao()
                        .findMostRecentClazzLogToEditUid(clazzUid)
                    navController.takeIf { mostRecentLogUid != 0L }?.navigate(
                        viewName = ClazzLogEditAttendanceViewModel.DEST_NAME,
                        args = mapOf(UstadView.ARG_ENTITY_UID to mostRecentLogUid.toString())
                    )
                    onDismissCreateNewOptions()
                }
            }
        }
    }

    fun onClickEntry(clazzLog: ClazzLog) {
        navController.navigate(
            viewName = ClazzLogEditAttendanceViewModel.DEST_NAME,
            args = mapOf(
                UstadView.ARG_ENTITY_UID to clazzLog.clazzLogUid.toString(),
                ARG_CLAZZUID to clazzUid.toString(),
            )
        )
    }

    companion object {

        const val DEST_NAME = "CourseLogListAttendanceView"

    }
}
