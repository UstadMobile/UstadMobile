package com.ustadmobile.view.contententry.getmetadata

import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState
import mui.material.CircularProgress
import mui.material.CircularProgressVariant
import mui.material.Stack
import mui.material.Typography
import react.FC
import react.Props
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.hooks.useUstadViewModel

external interface ContentEntryGetMetadataProps: Props {
    var uiState: ContentEntryGetMetadataUiState
}

val ContentEntryGetMetadataComponent = FC<ContentEntryGetMetadataProps> {props ->

    Stack {
        CircularProgress {
            variant = if(props.uiState.status.indeterminate) {
                CircularProgressVariant.indeterminate
            }else {
                CircularProgressVariant.determinate
            }
        }

        Typography {
            val strings = useStringProvider()

            + strings[MR.strings.uploading]
        }
    }

}

val ContentEntryGetMetadataScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryGetMetadataViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryGetMetadataUiState())

    ContentEntryGetMetadataComponent {
        uiState = uiStateVal
    }

}
