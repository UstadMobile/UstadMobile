package com.ustadmobile.view.signup

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.viewmodel.signup.SignupEnterUsernamePasswordUiState
import com.ustadmobile.core.viewmodel.signup.SignupEnterUsernamePasswordViewModel
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
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadPasswordTextField
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextField


external interface SignUpEnterUsernamePasswordProps : Props {
    var uiState: SignupEnterUsernamePasswordUiState
    var onPersonChanged: (Person?) -> Unit
    var onPasswordValueChange: (String) -> Unit
    var onClickSignUp: () -> Unit
}

val SignUpEnterUsernamePasswordScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        SignupEnterUsernamePasswordViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(SignupEnterUsernamePasswordUiState())

    SignUpEnterUsernamePasswordComponent2 {
        this.uiState = uiState
        onPersonChanged = viewModel::onEntityChanged
        onPasswordValueChange = viewModel::onPasswordChanged
        onClickSignUp = viewModel::onClickedSignupEnterUsernamePassword
    }
}

private val SignUpEnterUsernamePasswordComponent2 = FC<SignUpEnterUsernamePasswordProps> { props ->

    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            UstadTextField {
                id = "username"
                value = props.uiState.person?.username ?: ""
                label = ReactNode(strings[MR.strings.username])
                onTextChange = {
                    props.onPersonChanged(
                        props.uiState.person?.shallowCopy {
                            username = it
                        }
                    )
                }
                error = props.uiState.usernameError != null
                helperText = props.uiState.usernameError?.let { ReactNode(it) }
            }

            UstadPasswordTextField {
                id = "password"
                value = props.uiState.password ?: ""
                label = ReactNode(strings[MR.strings.password])
                onTextChange = {
                    props.onPasswordValueChange(it)
                }
                error = props.uiState.passwordError != null
                helperText = props.uiState.passwordError?.let { ReactNode(it) }
            }

            Box {
                sx {
                    height = 10.px
                }
            }

            Button {
                id = "signup_button"
                onClick = { props.onClickSignUp() }
                variant = ButtonVariant.contained
                + strings[MR.strings.signup].uppercase()
            }
        }
    }
}
