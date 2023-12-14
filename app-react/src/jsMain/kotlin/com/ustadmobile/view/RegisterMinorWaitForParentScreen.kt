package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.RegisterMinorWaitForParentUiState
import com.ustadmobile.mui.components.UstadDetailField
import web.cssom.px
import mui.icons.material.AccountCircle
import mui.icons.material.Key
import mui.icons.material.Visibility
import mui.icons.material.VisibilityOff
import mui.material.*
import mui.material.Button
import mui.system.responsive
import react.*

external interface RegisterMinorWaitForParentScreenProps : Props {

    var uiState: RegisterMinorWaitForParentUiState

    var onClickOk: () -> Unit

}

val RegisterMinorWaitForParentComponent2 = FC<RegisterMinorWaitForParentScreenProps> { props ->

    val strings = useStringProvider()

    var passwordVisible: Boolean by useState { false }

    val password = if(passwordVisible)
        props.uiState.password
    else
        "*****"

    Container {
        Stack {
            spacing = responsive(20.px)

            UstadDetailField {
                valueText = ReactNode(props.uiState.username)
                labelText = strings[MR.strings.username]
                icon = AccountCircle.create()
            }

            UstadDetailField {
                valueText = ReactNode(password)
                labelText = strings[MR.strings.password]
                icon = Key.create()

                secondaryActionContent = IconButton.create {
                    onClick = {
                        passwordVisible = !passwordVisible
                    }

                    if(!passwordVisible) {
                        Visibility { }
                    }else {
                        VisibilityOff { }
                    }
                }
            }

            Typography {
                + strings[MR.strings.we_sent_a_message_to_your_parent]
            }

            Button {
                variant = ButtonVariant.contained
                onClick = { props.onClickOk }

                + strings[MR.strings.ok]
            }
        }
    }
}

val RegisterMinorWaitForParentPreview = FC<Props> {

    val uiStateVal : RegisterMinorWaitForParentUiState by useState {
        RegisterMinorWaitForParentUiState(
            username = "new.username",
            password = "secret",
            parentContact = "parent@email.com"
        )
    }

    RegisterMinorWaitForParentComponent2 {
        uiState = uiStateVal
    }
}

