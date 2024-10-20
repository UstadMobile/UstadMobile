package com.ustadmobile.view.videocontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useOnUnloadEffect
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
import react.useMemo
import web.html.HTMLVideoElement

external interface VideoContentProps: Props {

    var uiState: VideoContentUiState

    var onPlayStateChanged: (VideoContentViewModel.MediaPlayState) -> Unit

    var onComplete: () -> Unit

    var onUnload: () -> Unit

}


fun HTMLVideoElement.mediaPlayState(): VideoContentViewModel.MediaPlayState {
    return VideoContentViewModel.MediaPlayState(
        timeInMillis = currentTime.toLong(),
        totalDuration = duration.toLong(),
        resumed = currentTime > 0 && !paused && !ended
    )
}


val VideoContentComponent = FC<VideoContentProps> { props ->

    useOnUnloadEffect {
        props.onUnload()
    }

    val manifestUrlVal = props.uiState.manifestUrl
    val firstSrcUrl = props.uiState.mediaContentInfo?.sources?.firstOrNull()?.uri
    val contentManifestMap = props.uiState.contentManifestMap

    val mediaSrc = useMemo(props.uiState.contentManifestMap, manifestUrlVal, firstSrcUrl) {
        if(contentManifestMap != null && firstSrcUrl != null && manifestUrlVal != null) {
            contentManifestMap.resolveUrl(manifestUrlVal, firstSrcUrl)
        }else {
            null
        }
    }

    Container {
        Stack {
            spacing = responsive(2)
            direction = responsive(StackDirection.column)

            mediaSrc?.also { mediaSrc ->
                video {
                    src = mediaSrc
                    controls = true
                    onTimeUpdate = {
                        props.onPlayStateChanged(it.currentTarget.mediaPlayState())
                    }

                    onPlay = {
                        props.onPlayStateChanged(it.currentTarget.mediaPlayState())
                    }

                    onPause = {
                        props.onPlayStateChanged(it.currentTarget.mediaPlayState())
                    }

                    onEnded = {
                        props.onPlayStateChanged(it.currentTarget.mediaPlayState())
                        props.onComplete()
                    }

                    css {
                        maxHeight = 80.vh
                        width = 100.pct
                        display = Display.block
                        marginLeft = Auto.auto
                        marginRight = Auto.auto
                    }

                    props.uiState.mediaContentInfo?.subtitles?.forEach { subtitle ->
                        UstadVideoContentSubtitleTrack {
                            subtitleTrack = subtitle
                            manifestUrl = props.uiState.manifestUrl
                            manifestMap = props.uiState.contentManifestMap
                        }
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
        onPlayStateChanged = viewModel::onPlayStateChanged
        onComplete = viewModel::onComplete
        onUnload = viewModel::onUnload
    }

}
