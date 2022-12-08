package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.HolidayCalendarDetailUIState
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.mui.components.UstadDetailField
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props

external interface HolidayCalendarDetailProps: Props{
    var uiState: HolidayCalendarDetailUIState
    var onItemClick: (HolidayCalendar) -> Unit
}

val HolidayCalendarDetailComponent2 = FC<HolidayCalendarDetailProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container{
        Stack{
            spacing = responsive(2)

            UstadDetailField{
                labelText = strings[MessageID.name]
                valueText = props.uiState.holidayCalendar?.umCalendarName ?: ""
            }

            props.uiState.calendarList?.forEach { item ->
                ListItem {

                    disablePadding = true

                    ListItemButton {
                        onClick = {
                            props.onItemClick(item)
                        }
                        ListItemText {
                            + (item.umCalendarName ?: "")
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
            calendarList = listOf(
                HolidayCalendar().apply {
                    umCalendarName = "first"
                },
                HolidayCalendar().apply {
                    umCalendarName = "second"
                }
            )
        )
    }

}