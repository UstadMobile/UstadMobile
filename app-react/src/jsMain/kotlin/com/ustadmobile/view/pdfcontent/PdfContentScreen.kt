package com.ustadmobile.view.pdfcontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentUiState
import com.ustadmobile.core.viewmodel.pdfcontent.PdfContentViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadFullSizeIframe
import react.FC
import react.Props

external interface PdfContentScreenProps : Props{
    var uiState: PdfContentUiState
}

val PdfContentComponent = FC<PdfContentScreenProps> { props ->
    props.uiState.pdfUrl?.also { pdfUrl ->
        UstadFullSizeIframe {
            src = pdfUrl
        }
    }
}

val PdfContentScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        PdfContentViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(PdfContentUiState())

    PdfContentComponent {
        uiState  = uiStateVal
    }
}

