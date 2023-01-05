package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.util.ext.personFullName
import com.ustadmobile.core.viewmodel.ClazzLogEditAttendanceUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.hooks.useTimeInOtherTimeZoneAsJsDate
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecord
import com.ustadmobile.lib.db.entities.ClazzLogAttendanceRecordWithPerson
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.mui.common.xs
import csstype.TextAlign
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface ClazzLogEditAttendanceScreenProps : Props {

    var uiState: ClazzLogEditAttendanceUiState

    var onClickMarkAllPresent: (Int) -> Unit

    var onClickMarkAllAbsent: (Int) -> Unit

    var onClickPreviousClazzLog: () -> Unit

    var onClickNextClazzLog: () -> Unit

    var onChangedAttendanceStatus: (Int) -> Unit

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
                    logDate = 1671975579000
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

            PagerView{
                uiState = props.uiState
                clazzLog = props.uiState.clazzLogsList
            }

            ListItemButton {
                onClick = {props
                    .onClickMarkAllPresent(ClazzLogAttendanceRecord.STATUS_ATTENDED)
                }

                + LibraryAddCheckOutlined.create()

                ListItemText {
                    primary = ReactNode(strings[MessageID.mark_all_absent])
                }
            }

            ListItemButton {
                onClick = {props
                    .onClickMarkAllAbsent(ClazzLogAttendanceRecord.STATUS_ABSENT)
                }

                + CheckBoxOutlined.create()

                ListItemText {
                    primary = ReactNode(strings[MessageID.mark_all_absent])
                }
            }

            List{
                props.uiState.clazzLogAttendanceRecordList.forEach { clazzLog ->
                    ClazzLogItemView {
                        clazzLogAttendance = clazzLog
                    }
                }
            }
        }
    }
}



external interface PagerViewProps : Props {

    var uiState: ClazzLogEditAttendanceUiState

    var onClickPreviousClazzLog: () -> Unit

    var onClickNextClazzLog: () -> Unit

    var clazzLog: List<ClazzLog>

}

private val PagerView = FC<PagerViewProps> { props ->

    val dateTime = useFormattedDateAndTime(
        props.clazzLog[0].logDate,
        props.uiState.timeZone
    )

    Grid {
        container = true

        Grid {
            item = true
            xs = 1

            Button {
                variant = ButtonVariant.text
                onClick = {
                    props.onClickPreviousClazzLog()
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
                    props.onClickNextClazzLog()
                }

                + ArrowForward.create()
            }
        }
    }
}



external interface ClazzLogItemViewProps : Props {

    var clazzLogAttendance: ClazzLogAttendanceRecordWithPerson

    var onChangedAttendanceStatus: (Int) -> Unit

}

private val ClazzLogItemView = FC<ClazzLogItemViewProps> { props ->

    ListItem{

        ListItemIcon {
            + mui.icons.material.Person.create()
        }

        ListItemText {
            primary = ReactNode(
                props.clazzLogAttendance.person?.personFullName() ?: ""
            )
        }

        secondaryAction = ButtonGroup.create {

            Button {
                variant = ButtonVariant.outlined

                + Done.create()
            }

            Button {
                variant = ButtonVariant.outlined

                + Close.create()
            }

            Button {
                variant = ButtonVariant.outlined

                + AccessTime.create()
            }
        }
    }
}