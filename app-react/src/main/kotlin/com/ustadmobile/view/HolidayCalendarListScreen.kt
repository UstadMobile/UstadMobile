package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.HolidayCalendarListUiState
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.HolidayCalendarWithNumEntries
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
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

    val strings = useStringsXml()

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
                                secondary = ReactNode(strings[MessageID.num_holidays].replace("%1\$s",
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
                    umCalendarName = "hol name 1"
                    umCalendarUid = 898787
                    numEntries = 4
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "hol name 2"
                    umCalendarUid = 8
                    numEntries = 3
                },
                HolidayCalendarWithNumEntries().apply {
                    umCalendarName = "hol name 3"
                    umCalendarUid = 80
                    numEntries = 2
                }
            )
        )
    }
}