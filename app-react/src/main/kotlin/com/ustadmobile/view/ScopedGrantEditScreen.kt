package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import mui.icons.material.Delete
import mui.material.*
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.create
import react.useState

external interface ScopedGrantEditScreenProps : Props {

    var uiState: ScopedGrantEditUiState

    var onClickEdit: () -> Unit

    var onClickDelete: () -> Unit

}

val ScopedGrantEditScreenComponent2 = FC<ScopedGrantEditScreenProps> { props ->

    Container {

        List{
            props.uiState.bitmaskList.forEach {
                ListItem{
                    Button {
                        variant = ButtonVariant.text
                        onClick = { props.onClickEdit }

                        Stack {
                            direction = responsive(StackDirection.column)

                            + ("Hello")

                            + (props.uiState.entity?.sgPermissions.toString())

                            Button {
                                variant = ButtonVariant.text
                                onClick = { props.onClickDelete }

                                + Delete.create()
                            }
                        }
                    }
                }
            }
        }
    }
}

val ScopedGrantEditScreenPreview = FC<Props> {

    val uiStateVar : ScopedGrantEditUiState by useState {
        ScopedGrantEditUiState(
            bitmaskList = listOf(
                BitmaskFlag(
                    messageId = MessageID.incident_id,
                    flagVal = 0
                )
            )
        )
    }

    ScopedGrantEditScreenComponent2 {
        uiState = uiStateVar
    }
}