package com.ustadmobile.view

import com.ustadmobile.core.viewmodel.AboutUiState
import csstype.px
import mui.material.Container
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import react.FC
import react.Props
import react.dom.html.IframeLoading
import react.dom.html.ReactHTML.iframe

external interface AboutProps: Props {
    var uiState: AboutUiState
}

val AboutComponent2 = FC<AboutProps> {  props ->
    Container{
        maxWidth = "lg"

        Stack{
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            Typography{
                + props.uiState.version.toString()
            }

            iframe{
                src = props.uiState.url
                allowFullScreen = true
                loading = IframeLoading.eager
                height = 650.0
                title = "About"
            }
        }
    }
}

val AboutScreenPreview = FC<Props> {
    AboutComponent2{
        uiState = AboutUiState(
            url = "https://www.ustadmobile.com",
            version = "v0.4.4 (#232) - Thu, 19 Jan 2023 11:00:43 UTC"
        )
    }
}