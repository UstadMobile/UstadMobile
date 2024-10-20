package com.ustadmobile.view.contententry.getsubtitle

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.contententry.getsubtitle.GetSubtitleUiState
import com.ustadmobile.core.viewmodel.contententry.getsubtitle.GetSubtitleViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadProgressOrErrorMessage
import react.FC
import react.Props

external interface GetSubtitleProps: Props {
    var uiState: GetSubtitleUiState
}

val GetSubtitleComponent = FC<GetSubtitleProps> { props ->
    UstadProgressOrErrorMessage {
        errorMessage = props.uiState.error
    }
}

val GetSubtitleScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        GetSubtitleViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(GetSubtitleUiState())

    GetSubtitleComponent {
        uiState = uiStateVal
    }
}
