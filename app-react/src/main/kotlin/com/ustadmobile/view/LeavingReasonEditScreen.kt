package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.LeavingReasonEditUiState
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadTextEditField
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface LeavingReasonEditScreenProps: Props {
    var uiState : LeavingReasonEditUiState
    var onLeavingReasonChange: (LeavingReason?)->Unit

}

val LeavingReasonEditScreen = FC<LeavingReasonEditScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

            UstadTextEditField {
                value = props.uiState.leavingReason?.leavingReasonTitle ?: ""
                label = strings[MessageID.description]
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
    LeavingReasonEditScreen {
        uiState = uiStateVar
        onLeavingReasonChange = {
            uiStateVar = uiStateVar.copy(leavingReason = it)
        }
    }
}