package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.ClazzLogEditAttendanceUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.common.xs
import csstype.TextAlign
import csstype.px
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

external interface ClazzLogEditAttendanceScreenProps : Props {

    var uiState: ClazzLogEditAttendanceUiState

    var onClickMarkAll: (status: Int) -> Unit

    var onChangeClazzLog: (ClazzLog) -> Unit

    var onClazzLogAttendanceChanged: (ClazzLogAttendanceRecordWithPerson) -> Unit

}

val ClazzLogEditAttendanceScreenPreview = FC<Props> {
    ClazzLogEditAttendanceScreenComponent2 {
        uiState = ClazzLogEditAttendanceUiState(
            clazzLogAttendanceRecordList = listOf(
                ClazzLogAttendanceRecordWithPerson().apply {
                    clazzLogAttendanceRecordUid = 0
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                    person = Person().apply {
                        firstNames = "Student Name"
                    }
                },
                ClazzLogAttendanceRecordWithPerson().apply {
                    clazzLogAttendanceRecordUid = 1
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ATTENDED
                    person = Person().apply {
                        firstNames = "Student Name"
                    }
                },
                ClazzLogAttendanceRecordWithPerson().apply {
                    clazzLogAttendanceRecordUid = 2
                    attendanceStatus = ClazzLogAttendanceRecord.STATUS_ABSENT
                    person = Person().apply {
                        firstNames = "Student Name"
                    }
                }
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

private val ClazzLogEditAttendanceScreenComponent2 = FC<ClazzLogEditAttendanceScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            PagerView{
                timeZone = props.uiState.timeZone
                list = props.uiState.clazzLogsList
                onChangeClazzLog = props.onChangeClazzLog
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
                            primary = ReactNode(strings[MessageID.mark_all_absent])
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
                            primary = ReactNode(strings[MessageID.mark_all_absent])
                        }
                    }
                }

                props.uiState.clazzLogAttendanceRecordList.forEach { clazzLogAttendance ->

                    ClazzLogItemView {
                        clazzLog = clazzLogAttendance
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

}

private val PagerView = FC<PagerViewProps> { props ->

    var currentClazzLog by useState { 0 }
    val dateTime = useFormattedDateAndTime(
        props.list[currentClazzLog].logDate,
        props.timeZone
    )

    Grid {
        container = true

        Grid {
            item = true
            xs = 1

            Button {
                variant = ButtonVariant.text
                onClick = {
                    if (currentClazzLog != 0){
                        currentClazzLog -= 1

                        props.onChangeClazzLog(props.list[currentClazzLog])
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

            Button {
                variant = ButtonVariant.text
                onClick = {
                    if (currentClazzLog < props.list.size-1){
                        currentClazzLog += 1

                        props.onChangeClazzLog(props.list[currentClazzLog])
                    }
                }

                + ArrowForward.create()
            }
        }
    }
}



external interface ClazzLogItemViewProps : Props {

    var clazzLog: ClazzLogAttendanceRecordWithPerson

    var onClazzLogAttendanceChanged: (ClazzLogAttendanceRecordWithPerson) -> Unit

    var fieldsEnabled: Boolean
}

private val STATUS_TO_ICON_MAP = mapOf(
    ClazzLogAttendanceRecord.STATUS_ATTENDED to Done,
    ClazzLogAttendanceRecord.STATUS_ABSENT to Close,
    ClazzLogAttendanceRecord.STATUS_PARTIAL to AccessTime
)

private val ClazzLogItemView = FC<ClazzLogItemViewProps> { props ->

    ListItem{

        ListItemButton {

            ListItemIcon {
                Icon {
                    + mui.icons.material.Person.create()
                }
            }

            ListItemText {
                primary = ReactNode(
                    props.clazzLog.person?.personFullName() ?: ""
                )
            }

        }

        secondaryAction = ButtonGroup.create {

            STATUS_TO_ICON_MAP.forEach { (status ,icon) ->
                ToggleButton {
                    disabled = !props.fieldsEnabled
                    selected = (props.clazzLog.attendanceStatus == status)

                    onChange = { _,_ ->
                        props.onClazzLogAttendanceChanged(
                            props.clazzLog.shallowCopy {
                                attendanceStatus = status
                            }
                        )
                    }

                    + icon.create()
                }
            }

        }
    }
}