package com.ustadmobile.view.login

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.util.ext.onTextChange
import web.cssom.px
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.mui.components.UstadLanguageSelect
import com.ustadmobile.mui.components.UstadPasswordTextField
import com.ustadmobile.mui.components.UstadPoweredByLink
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField


external interface LoginProps : Props {
    var uiState: LoginUiState
    var onClickLogin: () -> Unit
    var onClickCreateAccount: () -> Unit
    var onClickConnectAsGuest: () -> Unit
    var onUsernameValueChange: (String) -> Unit
    var onPasswordValueChange: (String) -> Unit
    var onChangeLanguage: (UstadMobileSystemCommon.UiLanguage) -> Unit
}

val LoginScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LoginViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(LoginUiState())

    LoginComponent2 {
        this.uiState = uiState
        onClickLogin = viewModel::onClickLogin
        onClickCreateAccount = viewModel::onClickCreateAccount
        onClickConnectAsGuest = viewModel::onClickConnectAsGuest
        onUsernameValueChange = viewModel::onUsernameChanged
        onPasswordValueChange = viewModel::onPasswordChanged
        onChangeLanguage = viewModel::onChangeLanguage
    }
}



private val LoginComponent2 = FC<LoginProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography {
                variant = TypographyVariant.h6
                + (props.uiState.loginIntentMessage ?: "")
            }

            UstadTextField {
                id = "username"
                value = props.uiState.username
                label = ReactNode(strings[MR.strings.username])
                onTextChange = {
                    props.onUsernameValueChange(it)
                }
                error = props.uiState.usernameError != null
                helperText = props.uiState.usernameError?.let { ReactNode(it) }
                disabled = !props.uiState.fieldsEnabled
            }

            UstadPasswordTextField {
                id = "password"
                value = props.uiState.password
                label = ReactNode(strings[MR.strings.password])
                onTextChange = {
                    props.onPasswordValueChange(it)
                }
                error = props.uiState.passwordError != null
                disabled = !props.uiState.fieldsEnabled
                helperText = props.uiState.passwordError?.let { ReactNode(it) }
                onKeyUp = {
                    if(it.key == "Enter") {
                        props.onClickLogin()
                    }
                }
            }

            Box{
                sx {
                    height = 10.px
                }
            }

            Typography {
                variant = TypographyVariant.h6
                + (props.uiState.errorMessage ?: "")
            }

            Button {
                id = "login_button"
                onClick = { props.onClickLogin() }
                variant = ButtonVariant.contained
                + strings[MR.strings.login].uppercase()
            }

            Box{
               sx {
                   height = 10.px
               }
            }

            UstadLanguageSelect {
                langList = props.uiState.languageList
                currentLanguage = props.uiState.currentLanguage
                onItemSelected = props.onChangeLanguage
                fullWidth = true
                id = "language_select"
            }


            if(props.uiState.connectAsGuestVisible) {
                Button {
                    id = "connect_as_guest_button"
                    onClick = { props.onClickConnectAsGuest() }
                    variant = ButtonVariant.outlined
                    + strings[MR.strings.connect_as_guest].uppercase()
                }
            }

            Typography {
                align = TypographyAlign.center
                variant = TypographyVariant.caption
                + props.uiState.versionInfo
            }

            if(props.uiState.showPoweredBy) {
                UstadPoweredByLink()
            }

        }
    }
}
