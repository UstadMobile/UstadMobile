package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.HolidayCalendarEditUiState
import com.ustadmobile.lib.db.entities.HolidayCalendar
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import kotlinx.css.px
import mui.icons.material.Add
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props

external interface HolidayCalendarEditProps: Props {
    var uiState: HolidayCalendarEditUiState
    var onAddItemClick: () -> Unit
    var onHolidayCalendarChange: (HolidayCalendar?) -> Unit
}

val HolidayCalendarEditComponent2 = FC<HolidayCalendarEditProps> { props ->

    val strings = useStringsXml()

    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(mui.material.StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.holidayCalendar?.umCalendarName ?: ""
                label = strings[MessageID.name]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onHolidayCalendarChange(
                        props.uiState.holidayCalendar?.shallowCopy {
                            umCalendarName = it
                        })
                }
            }

            ListItem {
                ListItemButton{
                    ListItemIcon {
                        Add{}
                    }

                    ListItemText{
                        + (strings[MessageID.add_a_holiday])
                    }

                    onClick = {
                        props.onAddItemClick()
                    }
                }
            }
        }
    }

}

val HolidayCalendarEditPreview = FC<Props> {
    HolidayCalendarEditComponent2{
        uiState = HolidayCalendarEditUiState(
            holidayCalendar = HolidayCalendar().apply {
                umCalendarName = "my cal"
            }
        )
    }
}