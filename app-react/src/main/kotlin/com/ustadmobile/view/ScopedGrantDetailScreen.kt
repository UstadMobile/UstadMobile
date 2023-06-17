package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.model.BitmaskFlag
import com.ustadmobile.core.view.ScopedGrantDetailView
import com.ustadmobile.core.viewmodel.scopedgrant.detail.ScopedGrantDetailUiState
import com.ustadmobile.core.viewmodel.scopedgrant.detail.ScopedGrantDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.view.components.UstadFab
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

        val strings = useStringsXml()

        props.uiState.bitmaskList.forEach { bitmask ->
            ListItem{
                ListItemSecondaryAction {
                    Icon {
                        ariaLabel = if(bitmask.enabled) {
                            strings[MessageID.enabled]
                        }else {
                            strings[MessageID.disabled]
                        }

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

val ScopedGrantDetailScreen = FC<Props>{
    val viewModel = useUstadViewModel{di, savedStateHandle ->
        ScopedGrantDetailViewModel(di, savedStateHandle)
    }

    val uiState by viewModel.uiState.collectAsState(ScopedGrantDetailUiState())

    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab{
        fabState = appState.fabState
    }

    ScopedGrantDetailComponent2{
        this.uiState = uiState

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