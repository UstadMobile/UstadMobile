package com.ustadmobile.view.clazz.detailoverview

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.Padding
import csstype.px
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
    val strings = useStringsXml()

    val fromTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.sceduleStartTime.toInt(),
    )

    val toTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.scheduleEndTime.toInt(),
    )

    val text = "${strings[ScheduleConstants
        .SCHEDULE_FREQUENCY_MESSAGE_ID_MAP[props.schedule.scheduleFrequency] ?: 0]} " +
        " ${strings[ScheduleConstants
            .DAY_MESSAGE_ID_MAP[props.schedule.scheduleDay] ?: 0]  } " +
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