package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.InviteStudentsViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadPersonAvatar
import csstype.JustifyContent
import com.ustadmobile.mui.common.justifyContent
import csstype.Padding
import csstype.px
import mui.icons.material.Add
import mui.material.Button
import mui.material.ButtonVariant
import mui.material.Card
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.Container
import mui.material.FormControlVariant
import mui.material.Icon
import mui.material.TextField
import mui.material.Typography
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface InviteStudentsProps : Props {

    var uiState: InviteStudentsUiState

    var onTextFieldChanged: (String) -> Unit

    var onClickAddRecipient: (String) -> Unit

    var onClickRemoveRecipient: (String) -> Unit

    var onClickCopyLink: () -> Unit

    var onClickShareLink: () -> Unit

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
        onClickCopyLink = viewModel::onClickCopyLink
        onClickShareLink = viewModel::onClickShareLink
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
                    onDelete = {
                        props.onClickRemoveRecipient(recipient)
                    }
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
                    fullWidth = true
                    onClick = {
                        props.onClickAddRecipient(props.uiState.textfield)
                    }

                    + Stack.create {
                        direction = responsive(StackDirection.row)
                        justifyContent = JustifyContent.spaceBetween

                        Stack {

                            Typography {
                                + "Add Receipent"
                            }
                            Typography {
                                + "another@gmail.com"
                            }
                        }

                        UstadPersonAvatar {
                            personUid = 1
                        }
                    }
                }
            }

            Card {
                sx {
                    padding = Padding(16.px, 17.px)
                }

               Stack {
                   direction = responsive(StackDirection.row)

                   Icon {
                       + Add.create()
                   }

                   Typography {
                       + "Class invitation link"
                   }
               }

                Stack {
                    direction = responsive(StackDirection.row)

                    Button {
                        variant = ButtonVariant.text
                        onClick = {
                            props.onClickShareLink()
                        }
                        + "Share"
                    }

                    Button {
                        variant = ButtonVariant.text
                        onClick = {
                            props.onClickCopyLink()
                        }
                        + "Copy link"
                    }
                }
            }
        }
    }
}