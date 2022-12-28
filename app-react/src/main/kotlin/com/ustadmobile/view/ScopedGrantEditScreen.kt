package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import mui.material.*
import react.*

external interface ScopedGrantEditScreenProps : Props {

    var uiState: ScopedGrantEditUiState

    var onChangedBitmask: (BitmaskFlag?) -> Unit

}

val ScopedGrantEditScreenComponent2 = FC<ScopedGrantEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {

        List{
            props.uiState.bitmaskList
                .forEach { bitmask ->
                    ListItem{
                        ListItemText {
                            primary = ReactNode(strings[bitmask.messageId])
                        }

                        secondaryAction  = Switch.create {
                            checked = bitmask.enabled
                            onChange = { _, isEnabled ->
                                props.onChangedBitmask(bitmask.copy(
                                    enabled = isEnabled
                                ))
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
                ),
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