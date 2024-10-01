package com.ustadmobile.view.person.addaccount

import com.ustadmobile.core.MR
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingUiState
import com.ustadmobile.core.viewmodel.AddAccountSelectNewOrExistingViewModel
import com.ustadmobile.mui.components.UstadLanguageSelect
import com.ustadmobile.mui.components.UstadStandardContainer
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.img
import web.cssom.px

external interface AddAccountSelectNewOrExistingProps : Props {
    var uiState: AddAccountSelectNewOrExistingUiState
    var onSetLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit
    var onClickNewUser: () -> Unit
    var onClickExistingUser: () -> Unit
}

val AddAccountSelectNewOrExistingScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        AddAccountSelectNewOrExistingViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(AddAccountSelectNewOrExistingUiState())

    AddAccountSelectNewOrExistingComponent2 {
        this.uiState = uiState
        onSetLanguage = viewModel::onLanguageSelected
        onClickNewUser = viewModel::onClickNewUser
        onClickExistingUser = viewModel::onClickExistingUser
    }
}

val AddAccountSelectNewOrExistingComponent2 = FC<AddAccountSelectNewOrExistingProps> { props ->
    val strings: StringProvider = useStringProvider()

    UstadStandardContainer {
        Stack {


            UstadLanguageSelect {
                langList = props.uiState.languageList
                currentLanguage = props.uiState.currentLanguage
                onItemSelected = props.onSetLanguage
                fullWidth = true
                id = "language_select"
            }
            Box {
                sx {
                    height = 10.px
                }
            }


            img {
                src = "assets/logo.svg"
                alt = "App Icon"
                height = 200.0
            }
            Box {
                sx {
                    height = 10.px
                }
            }
            Typography {
                +strings[MR.strings.app_name]
                variant = TypographyVariant.h6
                align = TypographyAlign.center
            }

            Box {
                sx {
                    height = 50.px
                }
            }

            Button {
                onClick = { props.onClickNewUser() }
                variant = ButtonVariant.outlined
                fullWidth = true
                +strings[MR.strings.new_user]
                id="new_user"
            }
            Box {
                sx {
                    height = 10.px
                }
            }
            Button {
                onClick = { props.onClickExistingUser() }
                variant = ButtonVariant.outlined
                fullWidth = true
                +strings[MR.strings.existing_user]
                id="existing_user"
            }
        }
    }
}

