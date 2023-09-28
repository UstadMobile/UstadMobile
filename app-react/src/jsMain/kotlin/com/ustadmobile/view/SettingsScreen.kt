package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.SettingsUiState
import com.ustadmobile.mui.components.UstadDetailField
import web.cssom.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Language
import mui.icons.material.CalendarMonth
import mui.icons.material.AccountBalance
import mui.icons.material.ExitToApp
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

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField {
                icon = Language.create()
                labelText = "English"
                valueText = ReactNode(strings[MR.strings.app_language])
                onClick = props.onClickAppLanguage
            }

            if (props.uiState.holidayCalendarVisible){
                UstadDetailField {
                    icon = CalendarMonth.create()
                    labelText = strings[MR.strings.holiday_calendars_desc]
                    valueText = ReactNode(strings[MR.strings.holiday_calendars])
                    onClick = props.onClickGoToHolidayCalendarList
                }
            }

            if (props.uiState.workspaceSettingsVisible){
                UstadDetailField {
                    icon = AccountBalance.create()
                    labelText = strings[MR.strings.manage_site_settings]
                    valueText = ReactNode(strings[MR.strings.site])
                    onClick = props.onClickWorkspace
                }
            }

            if (props.uiState.reasonLeavingVisible){
                UstadDetailField {
                    icon = ExitToApp.create()
                    labelText = strings[MR.strings.leaving_reason_manage]
                    valueText = ReactNode(strings[MR.strings.leaving_reason])
                    onClick = props.onClickLeavingReason
                }
            }

            if (props.uiState.langListVisible){
                UstadDetailField {
                    icon = Language.create()
                    labelText = strings[MR.strings.languages_description]
                    valueText = ReactNode(strings[MR.strings.languages])
                    onClick = props.onClickLangList
                }
            }
        }
    }
}