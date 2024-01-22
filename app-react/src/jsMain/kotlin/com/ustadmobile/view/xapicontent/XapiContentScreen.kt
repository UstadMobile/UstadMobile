package com.ustadmobile.view.xapicontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadFullSizeIframe
import react.FC
import react.Props


external interface XapiContentProps : Props {

    var uiState: XapiContentUiState

}

val XapiContentComponent = FC<XapiContentProps> { props ->
    val iframeSrc = props.uiState.url

    if(iframeSrc != null) {
        //To avoid all scrolling: put overflow: need to set overflow hidden on the root div or body element

        UstadFullSizeIframe {
            src = iframeSrc
            id = "xapi_content_frame"
        }
    }

}

val XapiContentScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        XapiContentViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(XapiContentUiState())

    XapiContentComponent {
        uiState = uiStateVal
    }
}
