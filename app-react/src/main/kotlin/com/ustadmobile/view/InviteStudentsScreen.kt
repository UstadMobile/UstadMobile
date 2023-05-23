package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.InviteStudentsViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadPersonAvatar
import csstype.px
import mui.material.Button
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.Container
import mui.material.FormControlVariant
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.TextField
import mui.material.Typography
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface InviteStudentsProps : Props {

    var uiState: InviteStudentsUiState

    var onTextFieldChanged: (String) -> Unit

    var onClickAddRecipient: (String) -> Unit

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
    }
}

private val InviteStudentsComponent2 = FC<InviteStudentsProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)


            props.uiState.recipients.forEach { recipient ->
                Chip {
                    label = ReactNode(recipient)
                    variant = ChipVariant.outlined
                    onClick = {}
                    onDelete = {}
                }
            }


            TextField {
                variant = FormControlVariant.outlined
                id = "textfield"
                value = props.uiState.textfield
                label = ReactNode(strings[MessageID.username])
                onTextChange = {
                    props.onTextFieldChanged(it)
                }
                disabled = !props.uiState.fieldsEnabled
            }

            if (props.uiState.textfield.isNotEmpty()){
                Button {
                    onClick = {
                        props.onClickAddRecipient(props.uiState.textfield)
                    }

                   + Stack.create {
                        direction = responsive(StackDirection.row)

                        Stack {

                            Typography {
                                + "Add Receipent"
                            }
                            Typography {
                                + "another@gmail.com"
                            }
                        }

                        UstadPersonAvatar {}
                    }
                }

            }
        }
    }
}