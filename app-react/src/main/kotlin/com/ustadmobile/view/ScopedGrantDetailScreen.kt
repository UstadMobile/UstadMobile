package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantDetailUiState
import mui.icons.material.Check
import mui.icons.material.Close
import mui.material.*
import react.FC
import react.Props
import react.ReactNode

external interface ScopedGrantDetailProps: Props {
    var uiState: ScopedGrantDetailUiState
}

val ScopedGrantDetailComponent2 = FC<ScopedGrantDetailProps> { props ->  

    Container{

        val strings = useStringsXml()

        props.uiState.bitmaskList.forEach { bitmask ->
            ListItem{
                ListItemSecondaryAction {
                    Icon {
                        if (bitmask.enabled) Check() else Close()
                    }
                }

                ListItemText {
                    primary = ReactNode(strings[bitmask.messageId])
                }
            }
        }

    }

}

val ScopedGrantDetailScreenPreview = FC<Props> {
    ScopedGrantDetailComponent2{
        uiState = ScopedGrantDetailUiState(
            bitmaskList = listOf(
                BitmaskFlag(
                    messageId = MessageID.incident_id,
                    flagVal = 0,
                    enabled = true
                ),
                BitmaskFlag(
                    messageId = MessageID.message,
                    flagVal = 0,
                    enabled = false
                )
            )
        )
    }
}