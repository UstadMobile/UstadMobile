package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.InviteStudentsUiState
import com.ustadmobile.core.viewmodel.InviteStudentsViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.util.ext.onTextChange
import csstype.px
import mui.material.Container
import mui.material.FormControlVariant
import mui.material.TextField
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface InviteStudentsProps : Props {

    var uiState: InviteStudentsUiState

    var onTextFieldChanged: (String) -> Unit

    var onClickAddRecipient: () -> Unit

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

        }
    }
}