package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import com.ustadmobile.mui.common.justifyContent
import csstype.Color
import csstype.JustifyContent
import csstype.MaxWidth
import csstype.px
import mui.material.*
import mui.system.Spacing
import mui.system.responsive
import mui.system.sx
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