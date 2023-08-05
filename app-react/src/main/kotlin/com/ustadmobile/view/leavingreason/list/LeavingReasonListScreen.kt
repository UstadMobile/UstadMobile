package com.ustadmobile.view.leavingreason.list

import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.StringsXml
import com.ustadmobile.core.viewmodel.leavingreason.list.LeavingReasonListUiState
import mui.system.Container
import mui.system.Stack
import mui.system.responsive
import react.FC
import react.Props
import react.useState

external interface LeavingReasonListScreenProps: Props {

    var uiState : LeavingReasonListUiState

}

val LeavingReasonListScreenComponent = FC<LeavingReasonListScreenProps> { props ->

    val strings: StringsXml = useStringsXml()

    Container {
        Stack {
            spacing = responsive(2)

        }
    }
}


val LeavingReasonListScreenPreview = FC<Props> {
    var uiStateVar by useState {

    }
    LeavingReasonListScreenComponent {

    }
}

val LeavingReasonListScreen = FC<Props> {

    LeavingReasonListScreenComponent {
    }
}