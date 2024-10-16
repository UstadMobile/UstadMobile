package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.HolidayCalendarDetailUIState
import com.ustadmobile.hooks.useFormattedDate
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.mui.components.UstadDetailField
import mui.icons.material.CalendarToday
import mui.material.*
import mui.system.responsive
import react.*

external interface HolidayCalendarDetailProps: Props{
    var uiState: HolidayCalendarDetailUIState
    var onItemClick: (Holiday) -> Unit
}

val HolidayCalendarDetailComponent2 = FC<HolidayCalendarDetailProps> { props ->

    val strings: StringProvider = useStringProvider()

    Container{
        Stack{
            spacing = responsive(2)

            UstadDetailField{
                labelText = strings[MR.strings.name_key]
                valueText = ReactNode(props.uiState.holidayCalendar?.umCalendarName ?: "")
                icon = CalendarToday.create()
            }

            List{
                props.uiState.holidayList.forEach { item ->
                    ListItem {

                        disablePadding = true

                        val holidayStart = useFormattedDate(item.holStartTime, "UTC")
                        val holidayEnd = useFormattedDate(item.holEndTime, "UTC")

                        ListItemButton {
                            onClick = {
                                props.onItemClick(item)
                            }
                            ListItemText {
                                primary = ReactNode(item.holName ?: "")
                                secondary = ReactNode("$holidayStart - $holidayEnd")
                            }
                        }
                    }
                }
            }
        }
    }

}

val HolidayCalendarDetailPreview = FC<Props>{

    HolidayCalendarDetailComponent2 {
        uiState = HolidayCalendarDetailUIState(
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "My Calendar"
            },
            holidayList = listOf(
                Holiday().apply {
                    holName = "Eid"
                    holStartTime = 1352958816
                    holEndTime = 1352958818
                }
            )
        )
    }

}