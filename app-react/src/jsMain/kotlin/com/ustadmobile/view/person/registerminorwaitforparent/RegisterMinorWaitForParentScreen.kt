package com.ustadmobile.view.person.registerminorwaitforparent

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentUiState
import com.ustadmobile.core.viewmodel.person.registerminorwaitforparent.RegisterMinorWaitForParentViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadDetailField
import com.ustadmobile.mui.components.UstadStandardContainer
import web.cssom.px
import mui.icons.material.AccountCircle as AccountCircleIcon
import mui.icons.material.Key as KeyIcon
import mui.icons.material.Visibility as VisibilityIcon
import mui.icons.material.VisibilityOff as VisibilityOffIcon
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

    UstadStandardContainer {
        Stack {
            spacing = responsive(20.px)

            UstadDetailField {
                valueText = ReactNode(props.uiState.username)
                labelText = strings[MR.strings.username]
                icon = AccountCircleIcon.create()
            }

            UstadDetailField {
                valueText = ReactNode(password)
                labelText = strings[MR.strings.password]
                icon = KeyIcon.create()

                secondaryActionContent = IconButton.create {
                    onClick = {
                        passwordVisible = !passwordVisible
                    }

                    if(!passwordVisible) {
                        VisibilityIcon()
                    }else {
                        VisibilityOffIcon()
                    }
                }
            }

            Typography {
                + strings.format(MR.strings.we_sent_a_message_to_your_parent, props.uiState.parentContact)
            }

            Button {
                variant = ButtonVariant.contained
                onClick = { props.onClickOk() }

                + strings[MR.strings.ok]
            }
        }
    }
}

val RegisterMinorWaitForParentScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        RegisterMinorWaitForParentViewModel(di, savedStateHandle)
    }
    val uiStateVal by viewModel.uiState.collectAsState(
        RegisterMinorWaitForParentUiState()
    )


    RegisterMinorWaitForParentComponent2 {
        uiState = uiStateVal
        onClickOk = viewModel::onClickOK
    }

}

@Suppress("unused")
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

