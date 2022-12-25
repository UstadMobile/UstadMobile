package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.HolidayCalendarDetailUIState
import com.ustadmobile.lib.db.entities.Holiday
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.mui.components.UstadDetailField
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.useMemo
import kotlin.js.Date

external interface HolidayCalendarDetailProps: Props{
    var uiState: HolidayCalendarDetailUIState
    var onItemClick: (Holiday) -> Unit
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

            List{
                props.uiState.holidayList?.forEach { item ->
                    ListItem {

                        disablePadding = true

                        val holidayStart = useMemo(item.holStartTime) {
                            Date(item.holStartTime).toLocaleDateString()
                        }
                        val holidayEnd = useMemo(item.holEndTime) {
                            Date(item.holEndTime).toLocaleDateString()
                        }

                        ListItemButton {
                            onClick = {
                                props.onItemClick(item)
                            }
                            ListItemText {
                                + (item.holName ?: "")
                            }

                            ListItemSecondaryAction {
                                ListItemText {
                                    + ("$holidayStart - $holidayEnd")
                                }
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