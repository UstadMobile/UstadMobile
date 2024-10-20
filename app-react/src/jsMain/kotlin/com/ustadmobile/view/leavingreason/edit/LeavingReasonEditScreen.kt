package com.ustadmobile.view.leavingreason.edit

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.LeavingReasonEditUiState
import com.ustadmobile.core.viewmodel.LeavingReasonEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.mui.components.UstadTextEditField
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface LeavingReasonEditScreenProps: Props {
    var uiState : LeavingReasonEditUiState
    var onLeavingReasonChange: (LeavingReason?)->Unit

}

val LeavingReasonEditScreenComponent = FC<LeavingReasonEditScreenProps> { props ->

    val strings: StringProvider = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)

            UstadTextEditField {
                value = props.uiState.leavingReason?.leavingReasonTitle ?: ""
                label = strings[MR.strings.description]
                error = props.uiState.reasonTitleError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onLeavingReasonChange(
                        props.uiState.leavingReason?.shallowCopy {
                            leavingReasonTitle = it
                        })
                }
            }
        }
    }
}


val LeavingReasonEditScreenPreview = FC<Props> {
    var uiStateVar by useState {
        LeavingReasonEditUiState(
            leavingReason = LeavingReason().apply {
                leavingReasonTitle = "Leaving because of something..."
            }
        )
    }
    LeavingReasonEditScreenComponent {
        uiState = uiStateVar
        onLeavingReasonChange = {
            uiStateVar = uiStateVar.copy(leavingReason = it)
        }
    }
}

val LeavingReasonEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        LeavingReasonEditViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(LeavingReasonEditUiState())

    LeavingReasonEditScreenComponent {
        uiState = uiStateVal
        onLeavingReasonChange = viewModel::onEntityChanged
    }
}
