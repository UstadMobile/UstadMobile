package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.HolidayCalendarListUiState
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import web.cssom.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface HolidayCalendarListProps: Props{
    var uiState: HolidayCalendarListUiState
    var onListItemClick: (HolidayCalendarWithNumEntries) -> Unit
}

val HolidayCalendarListComponent2 = FC<HolidayCalendarListProps> { props ->

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            List{
                props.uiState.holidayCalendarList.forEach {  holidayCalendar ->
                    ListItem {
                        disablePadding = true

                        ListItemButton{

                            onClick = {
                                props.onListItemClick(holidayCalendar)
                            }

                            ListItemText{
                                primary = ReactNode(holidayCalendar.umCalendarName ?: "")
                                secondary = ReactNode(strings[MR.strings.num_holidays].replace("%1\$s",
                                    holidayCalendar.numEntries.toString()))
                            }
                        }

                    }
                }
            }

        }
    }

}

val HolidayCalendarListScreenPreview = FC<Props> {
    HolidayCalendarListComponent2 {
        uiState = HolidayCalendarListUiState(
            holidayCalendarList = listOf(
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 1"
                    umCalendarUid = 898787
                    numEntries = 4
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 2"
                    umCalendarUid = 8
                    numEntries = 3
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "Calendar name 3"
                    umCalendarUid = 80
                    numEntries = 2
                }
            )
        )
    }
}