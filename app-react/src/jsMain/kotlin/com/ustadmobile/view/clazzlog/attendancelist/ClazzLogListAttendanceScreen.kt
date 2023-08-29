package com.ustadmobile.view.clazzlog.attendancelist

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.schedule.totalAttendeeStatusRecorded
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceUiState
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Color
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px
import js.core.jso
import mui.icons.material.CalendarToday as CalendarTodayIcon
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useState
import kotlin.math.max

external interface ClazzLogListAttendanceScreenProps : Props {

    var uiState: ClazzLogListAttendanceUiState

    var onClickEntry: (ClazzLog) -> Unit

    var onClickFilterChip: (MessageIdOption2?) -> Unit

}

val ClazzLogListAttendanceScreen = FC<Props> {
    val strings = useStringProvider()
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzLogListAttendanceViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzLogListAttendanceUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    var recordDialogVisible: Boolean by useState { false }

    ClazzLogListAttendanceScreenComponent {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
    }



    UstadFab {
        fabState = appState.fabState.copy(
            onClick = {
                if(uiStateVal.recordAttendanceOptions.size == 1) {
                    viewModel.onClickRecordAttendance(uiStateVal.recordAttendanceOptions.first())
                }else{
                    recordDialogVisible = true
                }
            }
        )
    }

    Dialog {
        open = recordDialogVisible
        onClose = {_, _ ->
            recordDialogVisible = false
        }

        List {
            uiStateVal.recordAttendanceOptions.forEach { option ->
                ListItem {
                    ListItemButton {
                        onClick = {
                            viewModel.onClickRecordAttendance(option)
                        }
                        ListItemText {
                            primary = ReactNode(strings[option.stringResource])
                        }
                    }
                }
            }
        }
    }
}

val ClazzLogListAttendanceScreenPreview = FC<Props> {

    ClazzLogListAttendanceScreenComponent {
        uiState = ClazzLogListAttendanceUiState(
            clazzLogsList = {
                ListPagingSource(listOf(
                    ClazzLog().apply {
                        clazzLogUid = 1
                        clazzLogNumPresent = 4
                        clazzLogNumPartial = 15
                        clazzLogNumAbsent = 10
                        logDate = 1673683347000
                    },
                    ClazzLog().apply {
                        clazzLogUid = 2
                        clazzLogNumPresent = 9
                        clazzLogNumPartial = 8
                        clazzLogNumAbsent = 45
                        logDate = 1673683347000
                    },
                    ClazzLog().apply {
                        clazzLogUid = 3
                        clazzLogNumPresent = 70
                        clazzLogNumPartial = 30
                        clazzLogNumAbsent = 10
                        logDate = 1673683347000
                    }
                ))
            }
        )
    }
}

private val ClazzLogListAttendanceScreenComponent = FC<ClazzLogListAttendanceScreenProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()
    val infiniteQueryResult = usePagingSource(props.uiState.clazzLogsList, true)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item {
                UstadListFilterChipsHeader.create {
                    filterOptions = props.uiState.viewIdToNumDays
                    selectedChipId = props.uiState.selectedChipId
                    enabled = props.uiState.fieldsEnabled
                    onClickFilterChip = props.onClickFilterChip
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { "${it.clazzLogUid}"}
            ) { clazzLogItem ->

                ClazzLogListItem.create {
                    clazzLog = clazzLogItem
                    onClick = props.onClickEntry
                    timeZoneId = props.uiState.timeZoneId
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }
    }
}

external interface ClazzLogListItemProps: Props {
    var clazzLog: ClazzLog?
    var onClick: (ClazzLog) -> Unit
    var timeZoneId: String
}

val ClazzLogListItem = FC<ClazzLogListItemProps> { props ->
    val dateTime = useFormattedDateAndTime(
        timeInMillis = props.clazzLog?.logDate ?: 0,
        timezoneId = props.timeZoneId
    )
    val strings = useStringProvider()

    ListItem{
        ListItemButton {
            onClick = {
                props.clazzLog?.also { props.onClick(it) }
            }

            ListItemIcon {
                Icon{
                    + CalendarTodayIcon.create()
                }
            }

            ListItemText {
                primary = ReactNode(dateTime)
                secondary = Stack.create {
                    Stack {
                        direction = responsive(StackDirection.row)
                        ClazzLogListItemAttendanceStatusBox {
                            numerator = props.clazzLog?.clazzLogNumPresent ?: 0
                            denominator = props.clazzLog?.totalAttendeeStatusRecorded ?: 1
                            color = Color("#4CAF50")
                        }
                        ClazzLogListItemAttendanceStatusBox {
                            numerator = props.clazzLog?.clazzLogNumPartial ?: 0
                            denominator = props.clazzLog?.totalAttendeeStatusRecorded ?: 1
                            color = Color("#ff9800")
                        }
                        ClazzLogListItemAttendanceStatusBox {
                            numerator = props.clazzLog?.clazzLogNumAbsent ?: 0
                            denominator = props.clazzLog?.totalAttendeeStatusRecorded ?: 1
                            color = Color("#b00020")
                        }
                    }

                    Typography {
                        props.clazzLog?.also { clazzLog ->
                            + (strings[MR.strings.three_num_items_with_name_with_comma]
                                .replace("%1\$d",
                                    (clazzLog.clazzLogNumPresent).toString())
                                .replace("%2\$s", strings[MR.strings.present])
                                .replace("%3\$d",
                                    (clazzLog.clazzLogNumPartial).toString())
                                .replace("%4\$s", strings[MR.strings.partial])
                                .replace("%5\$d",
                                    (clazzLog.clazzLogNumAbsent).toString())
                                .replace("%6\$s", strings[MR.strings.absent]))
                        }
                    }
                }
            }
        }
    }
}

private external interface ClazzLogListItemAttendanceStatusBoxProps: Props  {
    var numerator: Int
    var denominator: Int
    var color: Color
}

private val ClazzLogListItemAttendanceStatusBox = FC<ClazzLogListItemAttendanceStatusBoxProps> { props ->
    Box {
        sx{
            width = ((props.numerator * 100) / (max(props.denominator, 1))).pct
            height = 6.px
            backgroundColor = props.color
        }
    }
}
