package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.view.components.UstadBlankIcon
import web.cssom.Padding
import web.cssom.px
import mui.material.ListItem
import mui.material.ListItemText
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode

external interface ClazzDetailOverviewScheduleListItemProps: Props {

    var schedule: Schedule

}

val ClazzDetailOverviewScheduleListItem = FC<ClazzDetailOverviewScheduleListItemProps> { props ->
    val strings = useStringProvider()

    val fromTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.sceduleStartTime.toInt(),
    )

    val toTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.scheduleEndTime.toInt(),
    )


    val frequencyStr = ScheduleConstants
        .SCHEDULE_FREQUENCY_MESSAGE_ID_MAP[props.schedule.scheduleFrequency]?.let {
            strings[it]
    } ?: ""

    val dayStr = ScheduleConstants.DAY_MESSAGE_ID_MAP[props.schedule.scheduleDay]?.let {
        strings[it]
    } ?: ""

    val text = "$frequencyStr - $dayStr\n" +
        " $fromTimeFormatted - $toTimeFormatted "

    ListItem{
        sx {
            padding = Padding(
                top = 0.px,
                bottom = 0.px,
                left = 22.px,
                right = 0.px
            )
        }

        UstadBlankIcon()

        ListItemText{
            primary = ReactNode(text)
        }
    }
}