package com.ustadmobile.view.videocontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentUiState
import com.ustadmobile.core.viewmodel.videocontent.VideoContentViewModel
import com.ustadmobile.hooks.useUstadViewModel
import emotion.react.css
import mui.material.Container
import mui.material.Stack
import mui.material.StackDirection
import react.FC
import react.Props
import react.dom.html.ReactHTML.video
import web.cssom.Auto
import web.cssom.Display
import web.cssom.pct
import web.cssom.vh
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive

external interface VideoContentProps: Props {

    var uiState: VideoContentUiState

}

val VideoContentComponent = FC<VideoContentProps> { props ->

    Container {
        Stack {
            spacing = responsive(2)
            direction = responsive(StackDirection.column)

            props.uiState.mediaContentInfo?.also {
                video {
                    src = it.sources.first().url
                    controls = true
                    css {
                        maxHeight = 80.vh
                        width = 100.pct
                        display = Display.block
                        marginLeft = Auto.auto
                        marginRight = Auto.auto
                    }
                }
            }

            Typography {
                variant = TypographyVariant.body1

                + (props.uiState.contentEntry?.description ?: "")
            }
        }

    }


}

val VideoContentScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        VideoContentViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(VideoContentUiState())

    VideoContentComponent {
        uiState = uiStateVal
    }

}
