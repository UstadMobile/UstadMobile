package com.ustadmobile.view.settings

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.settings.SettingsUiState
import com.ustadmobile.core.viewmodel.settings.SettingsViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadDetailField2
import com.ustadmobile.mui.components.UstadStandardContainer
import web.cssom.px
//WARNING: DO NOT Replace with import mui.icons.material.[*] - Leads to severe IDE performance issues 10/Apr/23 https://youtrack.jetbrains.com/issue/KT-57897/Intellisense-and-code-analysis-is-extremely-slow-and-unusable-on-Kotlin-JS
import mui.icons.material.Language
import mui.icons.material.AccountBalance
import mui.icons.material.ExitToApp
import mui.material.Dialog
import mui.material.List
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
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

val SettingsScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SettingsViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(SettingsUiState())

    Dialog {
        open = uiStateVal.langDialogVisible
        onClose = { _, _ ->
            viewModel.onDismissLangDialog()
        }
        println("languages = ${uiStateVal.availableLanguages.joinToString { it.langCode }}")

        List {
            uiStateVal.availableLanguages.forEach { uiLang ->
                ListItem {
                    ListItemButton {
                        onClick = {
                            viewModel.onClickLang(uiLang)
                        }

                        ListItemText {
                            primary = ReactNode(uiLang.langDisplay)
                        }
                    }
                }
            }
        }
    }

    SettingsComponent2 {
        uiState = uiStateVal
        onClickWorkspace = viewModel::onClickSiteSettings
        onClickAppLanguage = viewModel::onClickLanguage
    }

}

@Suppress("unused")
val SettingsPreview = FC<Props> {

    val uiStateVar by useState {
        SettingsUiState(
            reasonLeavingVisible = true,
            holidayCalendarVisible = true,
            workspaceSettingsVisible = true,
            langDialogVisible = true,
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

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadDetailField2 {
                leadingContent = Language.create()
                labelContent = ReactNode(props.uiState.currentLanguage)
                valueContent = ReactNode(strings[MR.strings.app_language])
                onClick = props.onClickAppLanguage
            }

            if (props.uiState.workspaceSettingsVisible){
                UstadDetailField2 {
                    leadingContent = AccountBalance.create()
                    labelContent = ReactNode(strings[MR.strings.manage_site_settings])
                    valueContent = ReactNode(strings[MR.strings.site])
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
        }
    }
}