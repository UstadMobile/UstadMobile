package com.ustadmobile.view.clazz.inviteViaContact

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactUiState
import com.ustadmobile.core.viewmodel.clazz.inviteviaContact.InviteViaContactViewModel
import com.ustadmobile.hooks.useUstadViewModel
import react.Props
import react.FC

external interface InviteViaContactProps : Props {
    var uiState: InviteViaContactUiState
}


val InviteViaContactScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        InviteViaContactViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(InviteViaContactUiState())

    InviteViaContactComponent2 {
        uiState = uiStateVal
    }
}

private val InviteViaContactComponent2 = FC<InviteViaContactProps> { props ->

}
