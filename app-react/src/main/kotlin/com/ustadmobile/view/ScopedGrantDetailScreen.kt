package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantDetailUiState
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode

external interface ScopedGrantDetailProps: Props {
    var uiState: ScopedGrantDetailUiState
    var onItemClick: (BitmaskFlag) -> Unit
}

val ScopedGrantDetailComponent2 = FC<ScopedGrantDetailProps> { props ->  

    Container{

        val strings = useStringsXml()

        props.uiState.bitmaskList.forEach { bitmask ->
            ListItem{
                ListItemButton{
                    onClick = {
                        props.onItemClick(bitmask)
                    }

                    ListItemText {
                        primary = ReactNode(strings[bitmask.messageId])
                    }
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
                    flagVal = 0
                ),
                BitmaskFlag(
                    messageId = MessageID.message,
                    flagVal = 0
                )
            )
        )
    }
}