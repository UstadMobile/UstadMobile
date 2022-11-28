package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.ContentEntryEditUiState
import mui.material.*
import mui.system.Stack
import react.FC
import react.Props

external interface ContentEntryEditScreenProps : Props {

    var uiState: ContentEntryEditUiState

    var onClickUpdateContent: () -> Unit
}

val ContentEntryEditScreenPreview = FC<Props> {
    val strings = useStringsXml()
    ContentEntryEditScreenComponent2 {
        uiState = ContentEntryEditUiState(
            updateContentVisible = true
        )
    }
}

private val ContentEntryEditScreenComponent2 = FC<ContentEntryEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {

            if (props.uiState.updateContentVisible){
                Button {
                    onClick = { props.onClickUpdateContent }
                    variant = ButtonVariant.contained

                    +strings[MessageID.update_content].uppercase()
                }


            }

        }
    }
}