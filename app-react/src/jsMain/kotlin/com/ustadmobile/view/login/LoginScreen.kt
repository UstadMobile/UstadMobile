package com.ustadmobile.view.login

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.login.LoginUiState
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.mui.components.UstadTextEditField
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
import com.ustadmobile.core.impl.config.SupportedLanguagesConfig.Companion.LOCALE_USE_SYSTEM
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
        onClickConnectAsGuest = viewModel::handleConnectAsGuest
        onUsernameValueChange = viewModel::onUsernameChanged
        onPasswordValueChange = viewModel::onPasswordChanged
        onChangeLanguage = viewModel::onChangeLanguage
    }
}



private val LoginComponent2 = FC<LoginProps> { props ->

    val strings = useStringProvider()

    /*
     * The language setting for "use system language" is a blank string. This doesn't work with a
     * select field value. So we will convert a blank string to sys and back to blank
     */
    fun String.toLangSysVal() = if(this == LOCALE_USE_SYSTEM) "sys" else this

    fun String.fromLangSysVal() = if(this == "sys")
        LOCALE_USE_SYSTEM
    else
        this


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

            UstadTextEditField {
                id = "password"
                value = props.uiState.password
                label = strings[MR.strings.password]
                onChange = {
                    props.onPasswordValueChange(it)
                }
                error = props.uiState.passwordError
                enabled = props.uiState.fieldsEnabled
                password = true
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

            FormControl {
                fullWidth = true

                InputLabel {
                    id = "language_label"
                    +strings[MR.strings.language]
                }

                Select {
                    value = props.uiState.currentLanguage.langCode.toLangSysVal()
                    id = "language_select"
                    label = ReactNode(strings[MR.strings.language])
                    labelId = "language_label"
                    disabled = !props.uiState.fieldsEnabled
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedVal = ("" + event.target.value).fromLangSysVal()
                        val selectedLang = props.uiState.languageList.first {
                            it.langCode == selectedVal
                        }
                        props.onChangeLanguage(selectedLang)
                    }

                    props.uiState.languageList.forEach { lang ->
                        MenuItem {
                            value = lang.langCode.toLangSysVal()
                            + lang.langDisplay
                        }
                    }
                }
            }

            /*
            These items are not yet active
            Button {
                id = "create_account_button"
                onClick = { props.onClickCreateAccount() }
                variant = ButtonVariant.outlined
                + strings[MR.strings.create_account].uppercase()
            }

            Box{
                sx {
                    height = 10.px
                }
            }

            Button {
                id = "connect_as_guest_button"
                onClick = { props.onClickConnectAsGuest() }
                variant = ButtonVariant.outlined
                + strings[MR.strings.connect_as_guest].uppercase()
            }

            Box{
                sx {
                    height = 10.px
                }
            }

            Typography {
                align = TypographyAlign.center
                + props.uiState.versionInfo
            }
             */
        }
    }
}
