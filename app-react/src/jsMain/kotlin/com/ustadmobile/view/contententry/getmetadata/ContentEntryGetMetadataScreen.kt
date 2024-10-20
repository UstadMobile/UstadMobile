package com.ustadmobile.view.contententry.getmetadata

import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetadataStatus
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataUiState
import com.ustadmobile.core.viewmodel.contententry.getmetadata.ContentEntryGetMetadataViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadProgressOrErrorMessage
import react.FC
import react.Props

external interface ContentEntryGetMetadataProps: Props {
    var uiState: ContentEntryGetMetadataUiState
}

val ContentEntryGetMetadataComponent = FC<ContentEntryGetMetadataProps> {props ->
    UstadProgressOrErrorMessage {
        errorMessage = props.uiState.status.error
        progress = props.uiState.status.progress
        processedBytes = props.uiState.status.processedBytes
        totalBytes = props.uiState.status.totalBytes
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

val ContentEntryGetMetadataPreview = FC<Props> {
    ContentEntryGetMetadataComponent {
        uiState = ContentEntryGetMetadataUiState(
            status = ContentEntryGetMetadataStatus(
                indeterminate = true,
                error = "SNAFU"
            ),
        )
    }
}
