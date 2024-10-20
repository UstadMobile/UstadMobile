package com.ustadmobile.view.clazz.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ScheduleConstants
import com.ustadmobile.core.impl.locale.mapLookup
import com.ustadmobile.hooks.useFormattedTime
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.view.components.UstadBlankIcon
import mui.icons.material.Delete
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel

external interface ScheduleListItemProps : Props {

    var schedule: Schedule

    var onClickEditSchedule: (Schedule) -> Unit

    var onClickDeleteSchedule: (Schedule) -> Unit

}

val ScheduleListItem = FC<ScheduleListItemProps> { props ->

    val strings = useStringProvider()

    val fromTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.sceduleStartTime.toInt(),
    )

    val toTimeFormatted = useFormattedTime(
        timeInMillisSinceMidnight = props.schedule.scheduleEndTime.toInt(),
    )

    val text = strings.mapLookup(
        props.schedule.scheduleFrequency,
        ScheduleConstants.SCHEDULE_FREQUENCY_MESSAGE_ID_MAP
    ) + " " + strings.mapLookup(
        props.schedule.scheduleDay,
        ScheduleConstants.DAY_MESSAGE_ID_MAP
    ) + " $fromTimeFormatted - $toTimeFormatted "
    
    ListItem{
        secondaryAction = IconButton.create {
            onClick = { props.onClickDeleteSchedule(props.schedule) }
            ariaLabel = strings[MR.strings.delete]
            Delete { }
        }

        ListItemButton {
            onClick = { props.onClickEditSchedule(props.schedule) }
            ListItemIcon {
                UstadBlankIcon { }
            }

            ListItemText{
                primary = ReactNode(text)
            }
        }
    }
}
