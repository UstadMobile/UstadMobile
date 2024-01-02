package com.ustadmobile.mui.components

import com.ustadmobile.hooks.useMuiAppState
import emotion.react.css
import react.FC
import react.Props
import web.cssom.Contain
import web.cssom.Height
import web.cssom.None
import web.cssom.pct
import react.dom.html.ReactHTML.iframe
import web.cssom.Display


external interface UstadFullSizeIframeProps: Props {
    var src: String

    var id: String?
}

/**
 * Base iframe that will use the entire main screen space.
 */
val UstadFullSizeIframe = FC<UstadFullSizeIframeProps> { props ->
    val muiAppState = useMuiAppState()

    iframe {
        css {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            border = None.none
            contain = Contain.strict
            display = Display.block
        }

        src = props.src
        id = props.id

    }
}
