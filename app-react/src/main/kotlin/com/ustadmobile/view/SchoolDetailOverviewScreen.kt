package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.create

external interface SchoolDetailOverviewProps : Props {
    var uiState: SchoolDetailOverviewUiState
    var onClickSchoolCode: () -> Unit
    var onClickSchoolPhoneNumber: () -> Unit
    var onClickEmail: () -> Unit
    var onClickClazz: () -> Unit
}

val SchoolDetailOverviewScreenPreview = FC<Props> {
    SchoolDetailOverviewScreenComponent2 {
        uiState = SchoolDetailOverviewUiState(
            entity = SchoolWithHolidayCalendar().apply {
                schoolDesc = "School description over here."
                schoolCode = "abc123"
                schoolAddress = "Nairobi, Kenya"
                schoolPhoneNumber = "+971 44311111"
                schoolGender = 1
                schoolHolidayCalendarUid = 1
                holidayCalendar = HolidayCalendar().apply {
                    umCalendarName = "Kenya calendar A"
                }
                schoolEmailAddress = "info@schoola.com"
                schoolTimeZone = "Asia/Dubai"
            },
            schoolCodeVisible = true,
            clazzes = listOf(
                ClazzWithListDisplayDetails().apply {
                    clazzName = "Class A"
                    clazzDesc = "Class description"
                },
                ClazzWithListDisplayDetails().apply {
                    clazzName = "Class B"
                    clazzDesc = "Class description"
                },
                ClazzWithListDisplayDetails().apply {
                    clazzName = "Class C"
                    clazzDesc = "Class description"
                }
            )
        )
    }
}

val SchoolDetailOverviewScreenComponent2 = FC<SchoolDetailOverviewProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            if (props.uiState.schoolDescVisible) {
                Typography {
                    variant = TypographyVariant.h6

                    +(props.uiState.entity?.schoolDesc ?: "")
                }
            }

            if (props.uiState.schoolCodeLayoutVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.schoolCode ?: ""
                    labelText = strings[MessageID.school_code]
                    icon = ExitToApp.create()
                    onClick = props.onClickSchoolCode
                }
            }

            if (props.uiState.schoolAddressVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.schoolAddress ?: ""
                    labelText = strings[MessageID.address]
                    icon = LocationOn.create()
                }
            }

            if (props.uiState.schoolPhoneNumberVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.schoolPhoneNumber ?: ""
                    labelText = strings[MessageID.phone_number]
                    icon = Call.create()
                    onClick = props.onClickSchoolPhoneNumber
                }
            }

            if (props.uiState.calendarUidVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.holidayCalendar?.umCalendarName ?: ""
                    labelText = strings[MessageID.holiday_calendar]
                    icon = CalendarMonth.create()
                }
            }

            if (props.uiState.schoolEmailAddressVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.schoolAddress ?: ""
                    labelText = strings[MessageID.email]
                    icon = Email.create()
                    onClick = props.onClickEmail
                }
            }

            if (props.uiState.schoolTimeZoneVisible){
                UstadDetailField {
                    valueText = props.uiState.entity?.schoolTimeZone ?: ""
                    labelText = strings[MessageID.timezone]
                    icon = Language.create()
                }
            }

            Typography {
                variant = TypographyVariant.h6

                + strings[MessageID.classes]
            }

            Clazzes {
                uiState = props.uiState
                onClickClazz = props.onClickClazz
            }
        }
    }
}

private val Clazzes = FC<SchoolDetailOverviewProps> { props ->

    List{
        props.uiState.clazzes.forEach {
            ListItem{
                Stack {
                    direction = responsive(StackDirection.column)

                    Typography {
                        variant = TypographyVariant.h6
                        + (it.clazzName ?: "")
                    }

                    + (it.clazzDesc ?: "")
                }
            }
        }
    }
}