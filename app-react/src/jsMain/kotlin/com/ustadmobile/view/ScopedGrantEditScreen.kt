package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.ScopedGrantEditUiState
import mui.material.*
import react.*

external interface ScopedGrantEditScreenProps : Props {

    var uiState: ScopedGrantEditUiState

    var onChangedBitmask: (BitmaskFlag?) -> Unit

}

val ScopedGrantEditScreenComponent2 = FC<ScopedGrantEditScreenProps> { props ->

    val strings = useStringProvider()

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
                                primary = ReactNode(strings[bitmask.stringResource])
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
                    stringResource = MR.strings.permission_person_insert,
                    flagVal = 0
                ),
                BitmaskFlag(
                    stringResource = MR.strings.permission_person_update,
                    flagVal = 0
                )
            )
        )
    }

    ScopedGrantEditScreenComponent2 {
        uiState = uiStateVar
    }
}