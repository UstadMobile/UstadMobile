package com.ustadmobile.view.person.invitestudents

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.person.invitestudents.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.person.invitestudents.InviteStudentsViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.onTextChange
import csstype.px
import mui.material.Button
import mui.material.Chip
import mui.material.Container
import mui.material.FormControlVariant
import mui.material.TextField
import mui.material.Typography
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TypographyAlign
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface InviteStudentsProps : Props {

    var uiState: InviteStudentsUiState

    var onTextFieldChanged: (String) -> Unit

    var onClickAddRecipient: () -> Unit

    var onClickRemoveRecipient: (String) -> Unit

}

val InviteStudentsPreview = FC<Props> { props ->
    InviteStudentsComponent2 {
        uiState = InviteStudentsUiState()
    }
}

val InviteStudentsScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        InviteStudentsViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(InviteStudentsUiState())

    InviteStudentsComponent2 {
        this.uiState = uiState
        onTextFieldChanged = viewModel::onTextFieldChanged
        onClickAddRecipient = viewModel::onClickAddRecipient
        onClickRemoveRecipient = viewModel::onClickRemoveRecipient
    }
}

private val InviteStudentsComponent2 = FC<InviteStudentsProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            Container {
                props.uiState.recipients.forEach { recipient ->
                    Chip {
                        label = ReactNode(recipient)
                        onClick = {}
                        onDelete = {
                            props.onClickRemoveRecipient(recipient)
                        }
                    }
                }
            }

            TextField {
                variant = FormControlVariant.outlined
                id = "textField"
                value = props.uiState.textField
                label = ReactNode(strings[MessageID.phone_or_email])
                error = props.uiState.textFieldError != null
                helperText = props.uiState.textFieldError?.let { ReactNode(it) }
                onTextChange = {
                    props.onTextFieldChanged(it)
                }
                disabled = !props.uiState.fieldsEnabled
            }

            if (props.uiState.addRecipientVisible){
                Button {
                    fullWidth = true
                    onClick = {
                        props.onClickAddRecipient()
                    }

                    Container {
                        Typography {
                            + strings[MessageID.add_recipient]
                            align = TypographyAlign.justify
                        }
                        Typography {
                            + strings[MessageID.another_email]
                            align = TypographyAlign.justify
                        }
                    }
                }
            }
        }
    }
}