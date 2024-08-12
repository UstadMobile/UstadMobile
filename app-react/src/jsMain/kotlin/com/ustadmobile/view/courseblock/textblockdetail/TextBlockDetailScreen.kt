package com.ustadmobile.view.courseblock.textblockdetail

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailUiState
import com.ustadmobile.core.viewmodel.courseblock.textblockdetail.TextBlockDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadCourseBlockHeader
import com.ustadmobile.mui.components.UstadRawHtml
import com.ustadmobile.mui.components.UstadStandardContainer
import react.FC
import react.Props

external interface TextBlockDetailProps : Props{

    var uiState: TextBlockDetailUiState

}

val TextBlockDetailComponent = FC<TextBlockDetailProps> { props ->

    UstadCourseBlockHeader {
        block = props.uiState.courseBlock?.block
        picture = props.uiState.courseBlock?.picture
    }

    UstadRawHtml {
        html = props.uiState.courseBlock?.block?.cbDescription ?: ""
    }

}

val TextBlockDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        TextBlockDetailViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(TextBlockDetailUiState())

    UstadStandardContainer {
        TextBlockDetailComponent {
            uiState = uiStateVal
        }
    }


}
