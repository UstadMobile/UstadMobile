package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SchoolDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.ExitToApp
import mui.icons.material.LocationOn
import mui.icons.material.Call
import mui.icons.material.CalendarMonth
import mui.icons.material.Email
import mui.icons.material.Language
import mui.material.*
import mui.material.List
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create



external interface SchoolDetailOverviewProps : Props {

    var uiState: SchoolDetailOverviewUiState

    var onClickSchoolCode: () -> Unit

    var onClickSchoolPhoneNumber: () -> Unit

    var onClickEmail: () -> Unit

    var onClickClazz: (Clazz) -> Unit

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
                    valueText = ReactNode(props.uiState.entity?.schoolCode ?: "")
                    labelText = strings[MessageID.school_code]
                    icon = ExitToApp.create()
                    onClick = props.onClickSchoolCode
                }
            }

            if (props.uiState.schoolAddressVisible){
                UstadDetailField {
                    valueText = ReactNode(props.uiState.entity?.schoolAddress ?: "")
                    labelText = strings[MessageID.address]
                    icon = LocationOn.create()
                }
            }

            if (props.uiState.schoolPhoneNumberVisible){
                UstadDetailField {
                    valueText = ReactNode(props.uiState.entity?.schoolPhoneNumber ?: "")
                    labelText = strings[MessageID.phone_number]
                    icon = Call.create()
                    onClick = props.onClickSchoolPhoneNumber
                }
            }

            if (props.uiState.calendarUidVisible){
                UstadDetailField {
                    valueText = ReactNode(props.uiState.entity?.holidayCalendar?.umCalendarName ?: "")
                    labelText = strings[MessageID.holiday_calendar]
                    icon = CalendarMonth.create()
                }
            }

            if (props.uiState.schoolEmailAddressVisible){
                UstadDetailField {
                    valueText = ReactNode(props.uiState.entity?.schoolAddress ?: "")
                    labelText = strings[MessageID.email]
                    icon = Email.create()
                    onClick = props.onClickEmail
                }
            }

            if (props.uiState.schoolTimeZoneVisible){
                UstadDetailField {
                    valueText = ReactNode(props.uiState.entity?.schoolTimeZone ?: "")
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
        props.uiState.clazzes.forEach { clazzEntry ->
            ListItem{
                ListItemButton {
                    onClick = {
                        props.onClickClazz(clazzEntry)
                    }

                    ListItemText {
                        primary = ReactNode(clazzEntry.clazzName ?: "")
                        secondary = ReactNode(clazzEntry.clazzDesc ?: "")
                    }
                }
            }
        }
    }
}