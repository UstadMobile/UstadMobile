package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ClazzLogListAttendanceUiState
import com.ustadmobile.hooks.useFormattedDateAndTime
import com.ustadmobile.lib.db.entities.ClazzLog
import csstype.Color
import csstype.px
import kotlinx.datetime.TimeZone
import mui.icons.material.CalendarToday
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface ClazzLogListAttendanceScreenProps : Props {

    var uiState: ClazzLogListAttendanceUiState

    var onClickClazz: (ClazzLog) -> Unit

}

val ClazzLogListAttendanceScreenPreview = FC<Props> {

    ClazzLogListAttendanceScreenComponent2 {
        uiState = ClazzLogListAttendanceUiState(
            clazzLogsList = listOf(
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
            )
        )
    }
}

private val ClazzLogListAttendanceScreenComponent2 =
    FC<ClazzLogListAttendanceScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(20.px)

            List{
                props.uiState.clazzLogsList.forEach { clazzLog ->

                    val dateTime = useFormattedDateAndTime(
                        timeInMillis = clazzLog.logDate,
                        timezoneId = TimeZone.currentSystemDefault().id
                    )

                    val attendanceMap = mapOf(
                        clazzLog.clazzLogNumPresent to Color("#4CAF50"),
                        clazzLog.clazzLogNumPartial to Color("#ff9800"),
                        clazzLog.clazzLogNumAbsent  to Color("#b00020")
                    )

                    ListItem{
                        ListItemButton {
                            onClick = {
                                props.onClickClazz(clazzLog)
                            }

                            ListItemIcon {
                                Icon{
                                    + CalendarToday.create()
                                }
                            }

                            ListItemText {
                                primary = ReactNode(dateTime)
                                secondary = Stack.create {
                                    Stack {
                                        direction = responsive(StackDirection.row)

                                        attendanceMap.forEach { (attendanceStatus, color) ->

                                            Box {
                                                sx{
                                                  width = (attendanceStatus*100).px
                                                  height = 6.px
                                                  backgroundColor = color
                                                }
                                            }
                                        }
                                    }
                                    Typography {
                                        + (strings[MessageID.three_num_items_with_name_with_comma]

                                            .replace("%1\$d",
                                                (clazzLog.clazzLogNumPresent).toString())

                                            .replace("%2\$s", strings[MessageID.present])

                                            .replace("%3\$d",
                                                (clazzLog.clazzLogNumPartial).toString())

                                            .replace("%4\$s", strings[MessageID.partial])

                                            .replace("%5\$d",
                                                (clazzLog.clazzLogNumAbsent).toString())

                                            .replace("%6\$s", strings[MessageID.absent]))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
