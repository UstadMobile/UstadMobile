package com.ustadmobile.view.xapicontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentUiState
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.ReactHTML.iframe
import web.cssom.Height
import web.cssom.pct
import web.cssom.Contain
import web.cssom.None

external interface XapiContentProps : Props {

    var uiState: XapiContentUiState

}

val XapiContentComponent = FC<XapiContentProps> { props ->
    val muiAppState = useMuiAppState()
    val iframeSrc = props.uiState.url

    if(iframeSrc != null) {
        //To avoid all scrolling: put overflow: need to set overflow hidden on the root div or body element

        iframe {
            css {
                height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                width = 100.pct
                border = None.none
                contain = Contain.strict
            }

            src = iframeSrc

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
