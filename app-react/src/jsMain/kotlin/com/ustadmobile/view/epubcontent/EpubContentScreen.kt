package com.ustadmobile.view.epubcontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentUiState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import js.core.jso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.iframe
import react.useMemo
import react.useRef
import react.useState
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.None
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px
import web.dom.getComputedStyle
import web.html.HTMLIFrameElement

external interface EpubContentProps : Props{
    var uiState: EpubContentUiState
}

/**
 * The EpubContentComponent uses a Virtual List where each element uses an Iframe. the Iframe
 * element height needs to be set after the iframe loads to the actual height of the content. This
 * is done within EpubSpineItem.
 *
 * The loaded heights are stored within the defaultHeightMap so that when the user is scrolling
 * back the already known height can be used. The VirtualList will only render the elements that
 * are currently visible (this ensures efficient memory usage on epubs with many pages etc).
 *
 */
val EpubContentComponent = FC<EpubContentProps> { props ->
    val muiAppState = useMuiAppState()

    val defaultHeightMap = useMemo(dependencies = emptyArray()) {
        MutableStateFlow(mapOf<Int, String>())
    }

    val defaultHeights: Map<Int, String> by defaultHeightMap.collectAsState(emptyMap())

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            itemsIndexed(
                list = props.uiState.spineUrls,
                key = { _, index -> "spine_$index" }
            ) { item, index ->
                EpubSpineItem.create {
                    url = item
                    defaultHeight = defaultHeights[index]?.unsafeCast<Height>() ?: 600.px
                    onHeightChanged = { newHeight ->
                        defaultHeightMap.update { prev ->
                            buildMap {
                                putAll(prev)
                                put(index, newHeight.toString())
                            }
                        }
                    }
                }
            }
        }

        VirtualListOutlet()
    }
}

external interface EpubSpineItemProps: Props {

    var url: String

    var defaultHeight: Height

    var onHeightChanged: (Height) -> Unit

}

val EpubSpineItem = FC<EpubSpineItemProps> { props ->
    val iframeRef = useRef<HTMLIFrameElement>(null)
    var iframeHeight: Height by useState { props.defaultHeight }

    iframe {
        src = props.url
        ref = iframeRef

        onLoad = {
            iframeRef.current?.contentDocument?.body?.also { bodyEl ->
                val loadedHeight = bodyEl.offsetHeight
                val computedStyle = getComputedStyle(bodyEl)
                val calculatedHeight = "calc(${loadedHeight}px + ${computedStyle.marginTop} + ${computedStyle.marginBottom} + 16px)"
                    .unsafeCast<Height>()

                iframeHeight = calculatedHeight
                props.onHeightChanged(calculatedHeight)
            }
        }

        css {
            height = iframeHeight
            width = 100.pct
            border = None.none
            overflow = Overflow.hidden
            margin = 0.px
            padding = 0.px
            display = Display.block
        }
    }
}

val EpubContentScreen = FC<Props> {
    val epubViewModel = useUstadViewModel { di, savedStateHandle ->
        EpubContentViewModel(di, savedStateHandle)
    }

    val uiStateVal by epubViewModel.uiState.collectAsState(EpubContentUiState())

    EpubContentComponent {
        uiState = uiStateVal
    }
}

