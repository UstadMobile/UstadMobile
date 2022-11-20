package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.RegisterMinorWaitForParentUiState
import com.ustadmobile.mui.components.UstadDetailField
import csstype.px
import mui.icons.material.AccountCircle
import mui.icons.material.Key
import mui.material.*
import mui.material.Button
import mui.system.responsive
import react.FC
import react.Props
import react.create
import react.useState

external interface RegisterMinorWaitForParentScreenProps : Props {

    var uiState: RegisterMinorWaitForParentUiState

    var onClickTogglePasswordVisibility: () -> Unit

    var onClickOk: () -> Unit

}

val RegisterMinorWaitForParentComponent2 = FC<RegisterMinorWaitForParentScreenProps> { props ->

    val strings = useStringsXml()

    val password = if(props.uiState.passwordVisible)
        props.uiState.password
    else
        "*****"

    Container {
        Stack {
            spacing = responsive(20.px)

            UstadDetailField {
                valueText = props.uiState.username
                labelText = strings[MessageID.username]
                icon = AccountCircle.create()
            }

            UstadDetailField {
                valueText = password
                labelText = strings[MessageID.password]
                icon = Key.create()
            }

            Typography {
                + strings[MessageID.we_sent_a_message_to_your_parent]
            }

            Button {
                variant = ButtonVariant.contained
                onClick = { props.onClickOk }

                + strings[MessageID.ok]
            }
        }
    }
}

val RegisterMinorWaitForParentPreview = FC<Props> {

    val uiStateVar : RegisterMinorWaitForParentUiState by useState {
        RegisterMinorWaitForParentUiState(
            username = "new.username",
            password = "secret",
            passwordVisible = false
        )
    }

    RegisterMinorWaitForParentComponent2 {
        uiState = uiStateVar
    }
}

