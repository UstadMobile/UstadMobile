package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.LoginUiState
import mui.material.ButtonVariant.*
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.icons.material.*
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.*

external interface LoginProps : Props {
    var uiState: LoginUiState
    var onClickLogin: () -> Unit
    var onClickCreateAccount: () -> Unit
    var onClickConnectAsGuest: () -> Unit
    var onUsernameValueChange: (String) -> Unit
    var onPasswordValueChange: (String) -> Unit
}

val LoginPreview = FC<Props> {
    LoginComponent2 {
        uiState = LoginUiState()
    }
}

private val LoginComponent2 = FC<LoginProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography {
                variant = TypographyVariant.h6
                + (props.uiState.loginIntentMessage ?: "")
            }

            UstadTextEditField {
                value = props.uiState.username
                label = strings[MessageID.username]
                onChange = {
                    props.onUsernameValueChange(it)
                }
                error = props.uiState.usernameError
                enabled = props.uiState.fieldsEnabled
            }

            UstadTextEditField {
                value = props.uiState.password
                label = strings[MessageID.password]
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
                onClick = { props.onClickLogin }
                variant = contained
                + strings[MessageID.login].uppercase()
                color = ButtonColor.secondary
            }

            Box{
               sx {
                   height = 10.px
               }
            }

            Button {
                onClick = { props.onClickCreateAccount }
                variant = outlined
                + strings[MessageID.create_account].uppercase()
            }

            Box{
                sx {
                    height = 10.px
                }
            }

            Button {
                onClick = { props.onClickConnectAsGuest }
                variant = outlined
                + strings[MessageID.connect_as_guest].uppercase()
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
        }
    }
}
