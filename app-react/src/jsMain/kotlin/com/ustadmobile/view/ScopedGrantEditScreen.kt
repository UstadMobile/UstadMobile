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
                        ListItemButton {
                            onClick = {
                                props.onChangedBitmask(bitmask.copy(
                                    enabled = !bitmask.enabled
                                ))
                            }

                            ListItemText {
                                primary = ReactNode(strings[bitmask.messageId])
                            }

                            Switch {
                                checked = bitmask.enabled
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
                    messageId = MessageID.permission_person_insert,
                    flagVal = 0
                ),
                BitmaskFlag(
                    messageId = MessageID.permission_person_update,
                    flagVal = 0
                )
            )
        )
    }

    ScopedGrantEditScreenComponent2 {
        uiState = uiStateVar
    }
}