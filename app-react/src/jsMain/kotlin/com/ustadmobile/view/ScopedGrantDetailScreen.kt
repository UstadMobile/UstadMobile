package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantDetailUiState
import mui.icons.material.Check
import mui.icons.material.Close
import mui.material.*
import react.FC
import react.Props
import react.ReactNode
import react.dom.aria.ariaLabel

external interface ScopedGrantDetailProps: Props {
    var uiState: ScopedGrantDetailUiState
}

val ScopedGrantDetailComponent2 = FC<ScopedGrantDetailProps> { props ->  

    Container{

        val strings = useStringProvider()

        props.uiState.bitmaskList.forEach { bitmask ->
            ListItem{
                ListItemSecondaryAction {
                    Icon {
                        ariaLabel = if(bitmask.enabled) {
                            strings[MR.strings.enabled]
                        }else {
                            strings[MR.strings.disabled]
                        }

                        if (bitmask.enabled) Check() else Close()
                    }
                }

                ListItemText {
                    primary = ReactNode(strings[bitmask.stringResource])
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
                    stringResource = MR.strings.incident_id,
                    flagVal = 0,
                    enabled = true
                ),
                BitmaskFlag(
                    stringResource = MR.strings.message,
                    flagVal = 0,
                    enabled = false
                )
            )
        )
    }
}