package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.SettingsUiState
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.icons.material.*
import mui.material.Container
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.*

external interface SettingsProps : Props {

    var uiState: SettingsUiState

    var onClickAppLanguage: () -> Unit

    var onClickGoToHolidayCalendarList: () -> Unit

    var onClickWorkspace: () -> Unit

    var onClickLeavingReason: () -> Unit

    var onClickLangList: () -> Unit
}

val SettingsPreview = FC<Props> {

    val uiStateVar by useState {
        SettingsUiState(
            reasonLeavingVisible = true,
            holidayCalendarVisible = true,
            workspaceSettingsVisible = true,
            langListVisible = true,
        )
    }

    SettingsComponent2 {
        uiState = uiStateVar
        onClickAppLanguage =  { }
        onClickGoToHolidayCalendarList = { }
        onClickWorkspace = { }
        onClickLeavingReason = { }
        onClickLangList = { }
    }
}

val SettingsComponent2 = FC<SettingsProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                icon = Language.create()
                labelText = "English"
                valueText = ReactNode(strings[MessageID.app_language])
                onClick = props.onClickAppLanguage
            }

            if (props.uiState.holidayCalendarVisible){
                UstadDetailField {
                    icon = CalendarMonth.create()
                    labelText = strings[MessageID.holiday_calendars_desc]
                    valueText = ReactNode(strings[MessageID.holiday_calendars])
                    onClick = props.onClickGoToHolidayCalendarList
                }
            }

            if (props.uiState.workspaceSettingsVisible){
                UstadDetailField {
                    icon = AccountBalance.create()
                    labelText = strings[MessageID.manage_site_settings]
                    valueText = ReactNode(strings[MessageID.site])
                    onClick = props.onClickWorkspace
                }
            }

            if (props.uiState.reasonLeavingVisible){
                UstadDetailField {
                    icon = ExitToApp.create()
                    labelText = strings[MessageID.leaving_reason_manage]
                    valueText = ReactNode(strings[MessageID.leaving_reason])
                    onClick = props.onClickLeavingReason
                }
            }

            if (props.uiState.langListVisible){
                UstadDetailField {
                    icon = Language.create()
                    labelText = strings[MessageID.languages_description]
                    valueText = ReactNode(strings[MessageID.languages])
                    onClick = props.onClickLangList
                }
            }
        }
    }
}