package com.ustadmobile.view.clazzlog.editattendance

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceUiState
import com.ustadmobile.core.viewmodel.clazzlog.editattendance.ClazzLogEditAttendanceViewModel
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.composites.PersonAndClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadPersonAvatar
import dev.icerock.moko.resources.StringResource
import web.cssom.TextAlign
import web.cssom.px
import mui.material.*
import mui.material.List
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*

//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.LibraryAddCheckOutlined
import mui.icons.material.CheckBoxOutlined
import mui.icons.material.ArrowBack
import mui.icons.material.ArrowForward
import mui.icons.material.Done
import mui.icons.material.Close
import mui.icons.material.AccessTime
import mui.icons.material.SvgIconComponent
import react.dom.aria.ariaLabel
import web.cssom.Cursor

external interface ClazzLogEditAttendanceScreenProps : Props {

    var uiState: ClazzLogEditAttendanceUiState

    var onClickMarkAll: (status: Int) -> Unit

    var onChangeClazzLog: (ClazzLog) -> Unit

    var onClazzLogAttendanceChanged: (PersonAndClazzLogAttendanceRecord) -> Unit

}

@Suppress("unused")
val ClazzLogEditAttendanceScreenPreview = FC<Props> {
    ClazzLogEditAttendanceScreenComponent {
        uiState = ClazzLogEditAttendanceUiState(
            clazzLogAttendanceRecordList = listOf(
                PersonAndClazzLogAttendanceRecord(
                    person = Person().apply {
                        firstNames = "Student Name"
                    },
                    attendanceRecord = ClazzLogAttendanceRecord().apply {
                        clazzLogAttendanceRecordUid = 0
                        attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                    }
                ),
                PersonAndClazzLogAttendanceRecord(
                    person = Person().apply {
                        firstNames = "Student Name"
                    },
                    attendanceRecord = ClazzLogAttendanceRecord().apply {
                        clazzLogAttendanceRecordUid = 1
                        attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                    }
                ),
                PersonAndClazzLogAttendanceRecord(
                    person = Person().apply {
                        firstNames = "Student Name"
                    },
                    attendanceRecord = ClazzLogAttendanceRecord().apply {
                        clazzLogAttendanceRecordUid = 2
                        attendanceStatus = ClazzLogAttendanceRecord.STATUS_ABSENT
                    }
                )
            ),
            clazzLogsList = listOf(
                ClazzLog().apply {
                    logDate = 1671629979000
                },
                ClazzLog().apply {
                    logDate = 1655608510000
                },
                ClazzLog().apply {
                    logDate = 1671975579000
                }
            )
        )
    }
}

val ClazzLogEditAttendanceScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzLogEditAttendanceViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzLogEditAttendanceUiState())

    ClazzLogEditAttendanceScreenComponent {
        uiState = uiStateVal
        onClickMarkAll = viewModel::onClickMarkAll
        onClazzLogAttendanceChanged = viewModel::onClazzLogAttendanceChanged
        onChangeClazzLog = { viewModel.onChangeClazzLog(it) }
    }
}

private val ClazzLogEditAttendanceScreenComponent = FC<ClazzLogEditAttendanceScreenProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            PagerView{
                timeZone = props.uiState.timeZone
                list = props.uiState.clazzLogsList
                onChangeClazzLog = props.onChangeClazzLog
                currentIndex = props.uiState.currentClazzLogIndex
            }

            List {

                ListItem {
                    ListItemButton {
                        onClick = {
                            props.onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ATTENDED)
                        }

                        ListItemIcon {
                            + LibraryAddCheckOutlined.create()
                        }

                        ListItemText {
                            primary = ReactNode(strings[MR.strings.mark_all_present])
                        }
                    }
                }

                ListItem {
                    ListItemButton {
                        onClick = {
                            props.onClickMarkAll(ClazzLogAttendanceRecord.STATUS_ABSENT)
                        }

                        ListItemIcon {
                            + CheckBoxOutlined.create()
                        }

                        ListItemText {
                            primary = ReactNode(strings[MR.strings.mark_all_absent])
                        }
                    }
                }

                props.uiState.clazzLogAttendanceRecordList.forEach { clazzLogAttendance ->

                    ClazzLogItemView {
                        personAndRecord = clazzLogAttendance
                        onClazzLogAttendanceChanged = props.onClazzLogAttendanceChanged
                        fieldsEnabled = props.uiState.fieldsEnabled
                    }

                }
            }
        }
    }
}



external interface PagerViewProps : Props {

    var timeZone: String

    var onChangeClazzLog: (ClazzLog) -> Unit

    var list: List<ClazzLog>

    var currentIndex: Int

}

private val PagerView = FC<PagerViewProps> { props ->

    val strings = useStringProvider()

    val dateTime = useFormattedDateAndTime(
        timeInMillis = props.list.getOrNull(props.currentIndex)?.logDate ?: 0L,
        timezoneId = props.timeZone
    )

    Grid {
        container = true

        Grid {
            item = true
            xs = 1

            IconButton {
                disabled = props.currentIndex <= 0
                id = "prev_day_button"
                ariaLabel = strings[MR.strings.previous]
                onClick = {
                    val prevLog = props.list.getOrNull(props.currentIndex - 1)
                    if(prevLog != null) {
                        props.onChangeClazzLog(prevLog)
                    }
                }

                + ArrowBack.create()
            }
        }

        Grid {
            item = true
            xs = 10

            Typography {
                sx {
                    textAlign = TextAlign.center
                }
                + dateTime
            }
        }

        Grid {
            item = true
            xs = 1

            IconButton {
                disabled = props.currentIndex >= props.list.size -1
                id = "next_day_button"
                ariaLabel = strings[MR.strings.next]
                onClick = {
                    val nextLog = props.list.getOrNull(props.currentIndex + 1)
                    if(nextLog != null) {
                        props.onChangeClazzLog(nextLog)
                    }
                }

                + ArrowForward.create()
            }
        }
    }
}



external interface ClazzLogItemViewProps : Props {

    var personAndRecord: PersonAndClazzLogAttendanceRecord

    var onClazzLogAttendanceChanged: (PersonAndClazzLogAttendanceRecord) -> Unit

    var fieldsEnabled: Boolean
}

data class StatusIconAndLabel(
    val status: Int,
    val icon: SvgIconComponent,
    val labelStringResource: StringResource,
)

private val STATUS_AND_ICONS = listOf(
    StatusIconAndLabel(ClazzLogAttendanceRecord.STATUS_ATTENDED, Done, MR.strings.present),
    StatusIconAndLabel(ClazzLogAttendanceRecord.STATUS_ABSENT, Close, MR.strings.absent),
    StatusIconAndLabel(ClazzLogAttendanceRecord.STATUS_PARTIAL, AccessTime, MR.strings.partial)
)


private val ClazzLogItemView = FC<ClazzLogItemViewProps> { props ->
    val strings = useStringProvider()

    ListItem{

        ListItemButton {
            sx {
                cursor = Cursor.default
            }

            ListItemIcon {
                UstadPersonAvatar {
                    personName = props.personAndRecord.person?.fullName()
                    pictureUri = props.personAndRecord.personPicture?.personPictureThumbnailUri
                }
            }

            ListItemText {
                primary = ReactNode(
                    props.personAndRecord.person?.personFullName() ?: ""
                )
            }

        }

        secondaryAction = ButtonGroup.create {

            STATUS_AND_ICONS.forEach { (status, icon, labelMessageId) ->
                Tooltip{
                    title = ReactNode(strings[labelMessageId])
                    ToggleButton {
                        disabled = !props.fieldsEnabled
                        selected = (props.personAndRecord.attendanceRecord?.attendanceStatus == status)
                        ariaLabel = strings[labelMessageId]

                        onChange = { _,_ ->
                            props.onClazzLogAttendanceChanged(
                                props.personAndRecord.copy(
                                    person = props.personAndRecord.person,
                                    attendanceRecord = props.personAndRecord.attendanceRecord?.shallowCopy {
                                        attendanceStatus = status
                                    }
                                )
                            )
                        }

                        + icon.create()
                    }
                }
            }
        }
    }
}
