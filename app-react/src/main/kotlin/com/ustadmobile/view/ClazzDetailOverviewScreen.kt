package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.util.MS_PER_HOUR
import com.ustadmobile.core.util.MS_PER_MIN
import com.ustadmobile.core.viewmodel.ClazzDetailOverviewUiState
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.ClazzWithDisplayDetails
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.lib.db.entities.School
import com.ustadmobile.lib.db.entities.Schedule
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.*
import react.dom.aria.ariaLabel

external interface ClazzDetailOverviewProps : Props {

    var uiState: ClazzDetailOverviewUiState

    var onClickClassCode: (String) -> Unit

}

val ClazzDetailOverviewComponent2 = FC<ClazzDetailOverviewProps> { props ->

    val strings = useStringsXml()

    val numMembers = strings[MessageID.x_teachers_y_students]
        .replace("%1\$d", (props.uiState.clazz?.numTeachers ?: 0).toString())
        .replace("%2\$d", (props.uiState.clazz?.numStudents ?: 0).toString())

    val clazzStartTime = useFormattedTime(
        timeInMillisSinceMidnight = (props.uiState.clazz?.clazzStartTime ?: 0).toInt(),
    )

    val clazzEndTime = useFormattedTime(
        timeInMillisSinceMidnight = (props.uiState.clazz?.clazzEndTime ?: 0).toInt(),
    )

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography{
                + (props.uiState.clazz?.clazzDesc ?: "")
            }

            UstadDetailField {
                icon = Group.create()
                valueText = numMembers
                labelText = strings[MessageID.members]
            }

            UstadDetailField {
                icon = Login.create()
                valueText = numMembers
                labelText = strings[MessageID.class_code]
                onClick = {
                    props.onClickClassCode(props.uiState.clazz?.clazzCode ?: "")
                }
            }

            if (props.uiState.clazzSchoolUidVisible){
                TextImageRow {
                    icon = mui.icons.material.School
                    text = props.uiState.clazz?.clazzSchool?.schoolName ?: ""
                }
            }

            if (props.uiState.clazzDateVisible){
                TextImageRow {
                    icon = Event
                    text = "$clazzStartTime - $clazzEndTime"
                }
            }

            if (props.uiState.clazzHolidayCalendarVisible){
                TextImageRow {
                    icon = Event
                    text = props.uiState.clazz?.clazzHolidayCalendar?.umCalendarName ?: ""
                }
            }

            props.uiState.scheduleList.forEach { schedule ->
                val fromTimeFormatted = useFormattedTime(
                    timeInMillisSinceMidnight = schedule.sceduleStartTime.toInt(),
                )

                val toTimeFormatted = useFormattedTime(
                    timeInMillisSinceMidnight = schedule.scheduleEndTime.toInt(),
                )

                val text = "${strings[ScheduleConstants.SCHEDULE_FREQUENCY_MESSAGE_ID_MAP[schedule.scheduleFrequency] ?: 0]} " +
                        " ${strings[ScheduleConstants.DAY_MESSAGE_ID_MAP[schedule.scheduleDay] ?: 0]  } " +
                        " $fromTimeFormatted - $toTimeFormatted "

                ListItem{
                    ListItemText{
                        primary = ReactNode(text)
                    }
                }
            }
        }
    }
}

external interface TextImageRowProps : Props {

    var icon: SvgIconComponent

    var text: String

}

private val TextImageRow = FC<TextImageRowProps> { props ->

    Stack {
        direction = responsive(StackDirection.row)

        Icon{
            + props.icon.create()
        }

        Typography {
            + props.text
        }
    }
}

val ClazzDetailOverviewScreenPreview = FC<Props> {
    ClazzDetailOverviewComponent2 {
        uiState = ClazzDetailOverviewUiState(
            clazz = ClazzWithDisplayDetails().apply {
                clazzDesc = "Description"
                clazzCode = "abc123"
                clazzSchoolUid = 1
                clazzStartTime = ((14 * MS_PER_HOUR) + (30 * MS_PER_MIN)).toLong()
                clazzEndTime = 0
                clazzSchool = School().apply {
                    schoolName = "School Name"
                }
                clazzHolidayCalendar = HolidayCalendar().apply {
                    umCalendarName = "Holiday Calendar"
                }
            },
            scheduleList = listOf(
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                },
                Schedule().apply {
                    sceduleStartTime = 0
                    scheduleEndTime = 0
                }
            )
        )
    }
}
