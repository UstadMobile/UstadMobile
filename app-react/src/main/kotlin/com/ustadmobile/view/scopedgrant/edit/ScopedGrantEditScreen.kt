package com.ustadmobile.view.scopedgrant.edit

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.viewmodel.scopedgrant.edit.ScopedGrantEditUiState
import com.ustadmobile.core.viewmodel.scopedgrant.edit.ScopedGrantEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import mui.material.*
import react.*

external interface ScopedGrantEditScreenProps : Props {

    var uiState: ScopedGrantEditUiState

    var onToggleBitmask: (BitmaskFlag?) -> Unit

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
                                props.onToggleBitmask(bitmask)
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


val ScopedGrantEditScreen = FC<Props> {
    val viewModel = useUstadViewModel{ di, savedStateHandle ->
        ScopedGrantEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(ScopedGrantEditUiState())

    ScopedGrantEditScreenComponent2{
        uiState = uiStateVar
        onToggleBitmask = viewModel::onToggleBitmask
    }
}