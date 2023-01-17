package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.viewmodel.JoinWithCodeUiState
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props

external interface JoinWithCodeScreenProps : Props {

    var uiState: JoinWithCodeUiState

    var onCodeValueChange: (String) -> Unit

    var onClickDone: () -> Unit

}

val JoinWithCodeScreenPreview = FC<Props> {
    JoinWithCodeScreenComponent2 {
        uiState = JoinWithCodeUiState(
            buttonLabel = "join_class"
        )
    }
}

private val JoinWithCodeScreenComponent2 = FC<JoinWithCodeScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography {
                + strings[MessageID.join_code_instructions]
            }

            UstadTextEditField {
                value = props.uiState.code
                label = strings[MessageID.entity_code]
                    .replace("%1\$s", props.uiState.entityType)
                error = props.uiState.codeError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onCodeValueChange(it)
                }
            }


            Button {
                onClick = { props.onClickDone }
                variant = ButtonVariant.contained
                disabled = !props.uiState.fieldsEnabled
                + props.uiState.buttonLabel.uppercase()
            }
        }
    }
}