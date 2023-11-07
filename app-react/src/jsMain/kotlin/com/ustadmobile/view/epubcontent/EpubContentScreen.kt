package com.ustadmobile.view.epubcontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useLaunchedEffect
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.ext.forEach
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentUiState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubScrollCommand
import com.ustadmobile.core.viewmodel.epubcontent.EpubTocItem
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListContext
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import js.core.jso
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import mui.material.Box
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.IconButton
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemSecondaryAction
import mui.material.ListItemText
import mui.material.Tooltip
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.iframe
import react.useEffect
import react.useMemo
import react.useRef
import react.useRequiredContext
import react.useState
import tanstack.virtual.core.ScrollAlignment
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.None
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px
import web.cssom.vw
import web.dom.getComputedStyle
import web.html.HTMLIFrameElement
import web.scroll.ScrollBehavior
import web.uievents.CLICK
import kotlin.math.roundToInt
import mui.material.List as MuiList
import mui.icons.material.KeyboardArrowUp as KeyboardArrowUpIcon
import mui.icons.material.KeyboardArrowDown as KeyboardArrowDownIcon
import com.ustadmobile.core.MR
import react.dom.aria.ariaLabel
import react.dom.html.ReactHTML.img
import web.cssom.TextAlign

external interface EpubContentProps : Props{
    var uiState: EpubContentUiState

    var onClickLink: (baseUrl: String, href: String) -> Unit

    var scrollToCommands: Flow<EpubScrollCommand>

    var onDismissTableOfContents: () -> Unit

    var onClickTocItem: (EpubTocItem) -> Unit

    var onClickToggleTogItem: (EpubTocItem) -> Unit
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
 * Scrolling commands have to be handled using the TanStack virtualizer. The target destination
 * might not be in the DOM depending on the user's scroll position. Upon receiving a scroll command:
 *  1) EpubScrollComponent will invoke the tanstack virtualizer scroll to position
 *  2) EpubSpineItemComponent will collect the scroll command (if it is the target). Once the
 *     iframe has loaded, if there is any hash component, it will (via the scrollByFunction)
 *     pass the offset from the top of the spine item to the hash component. EpubScrollComponent
 *     will invoke tanstack virtualizer's scrollToOffset
 *
 */
val EpubContentComponent = FC<EpubContentProps> { props ->
    val muiAppState = useMuiAppState()
    val theme by useRequiredContext(ThemeContext)
    val strings = useStringProvider()


    val defaultHeightMap = useMemo(dependencies = emptyArray()) {
        MutableStateFlow(mapOf<Int, String>())
    }

    val defaultHeights: Map<Int, String> by defaultHeightMap.collectAsState(emptyMap())

    val scrollByCommandFlow = useMemo(dependencies = emptyArray()) {
        MutableSharedFlow<Int>(
            replay = 1,
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    }

    fun onScrollBy(amount: Int) {
        scrollByCommandFlow.tryEmit(amount)
    }

    Drawer {
        anchor = DrawerAnchor.right
        open = props.uiState.tableOfContentsOpen
        onClose = { _, _ ->
            props.onDismissTableOfContents()
        }

        MuiList {
            sx {
                maxWidth = 90.vw
            }

            Box {
                sx {
                    paddingTop = muiAppState.appBarHeight.px
                    textAlign = TextAlign.center
                }

                props.uiState.coverImageUrl?.also { coverUrl ->
                    img {
                        src = coverUrl
                        css {
                            maxWidth = 300.px
                            maxHeight = 300.px
                            paddingTop = theme.spacing(2)
                        }
                    }
                }

            }

            props.uiState.tableOfContentToDisplay.forEach { tocItem ->
                ListItem {
                    key = "toc_${tocItem.uid}"

                    ListItemButton {
                        sx {
                            paddingLeft = theme.spacing(2 + (tocItem.indentLevel * 2))
                        }
                        onClick = {
                            props.onClickTocItem(tocItem)
                        }

                        ListItemText {
                            primary = ReactNode(tocItem.label)
                        }
                    }

                    if(tocItem.hasChildren) {
                        ListItemSecondaryAction {
                            val collapsed = tocItem.uid in props.uiState.collapsedTocUids
                            val text = strings[if(collapsed) MR.strings.expand else MR.strings.collapse]
                            Tooltip {
                                title = ReactNode(text)

                                IconButton {
                                    onClick = {
                                        props.onClickToggleTogItem(tocItem)
                                    }
                                    ariaLabel = text

                                    if(collapsed) {
                                        KeyboardArrowDownIcon()
                                    }else {
                                        KeyboardArrowUpIcon()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
                    itemIndex = index
                    defaultHeight = defaultHeights[index]?.unsafeCast<Height>() ?: 600.px
                    onHeightChanged = { newHeight ->
                        defaultHeightMap.update { prev ->
                            buildMap {
                                putAll(prev)
                                put(index, newHeight.toString())
                            }
                        }
                    }
                    scrollByFunction = ::onScrollBy
                    onClickLink = { href ->
                        props.onClickLink(item, href)
                    }
                    scrollCommands = props.scrollToCommands
                }
            }
        }

        VirtualListOutlet()

        EpubScrollComponent {
            scrollToCommands = props.scrollToCommands
            scrollByCommands = scrollByCommandFlow
        }
    }
}

external interface EpubScrollProps : Props {
    var scrollToCommands: Flow<EpubScrollCommand>

    var scrollByCommands: Flow<Int>
}

val EpubScrollComponent = FC<EpubScrollProps> { props ->
    //EpubScrollComponent has to be a child component of VirtualList in order to use  VirtualListContext
    val virtualizerContext = useRequiredContext(VirtualListContext)

    /**
     * The scroll offset that the virtualizer needs to hit in order to reach the spine index as per
     * the EpubScrollCommand
     */
    var spineIndexOffsetTarget by useState { -1 }

    /**
     * The delta from the top of the spine page to the hash element (as provided by
     */
    var hashIndexDelta by useState { -1 }

    useLaunchedEffect(props.scrollToCommands) {
        props.scrollToCommands.collect { command ->
            val offsetTarget = virtualizerContext.virtualizer.getOffsetForIndex(
                command.spineIndex, ScrollAlignment.start
            )
            val offsetAsInt = offsetTarget.component1().toInt()
            spineIndexOffsetTarget = offsetAsInt
            virtualizerContext.virtualizer.scrollToOffset(offsetAsInt, jso {
                align = ScrollAlignment.start
                behavior = ScrollBehavior.instant
            })
        }
    }

    useLaunchedEffect(props.scrollByCommands) {
        props.scrollByCommands.collect { scrollAmount ->
            hashIndexDelta = scrollAmount
        }
    }

    /*
     * Once the virtualizer has finished scrolling to the given spineIndexOffsetTarget, if there is
     * a pending hashIndexDelta, then scroll by that amount so that we (finally) reach the exact
     * target.
     */
    useEffect(
        dependencies = arrayOf(
            spineIndexOffsetTarget,
            hashIndexDelta,
            virtualizerContext.virtualizer.isScrolling
        )
    ) {
        if(!virtualizerContext.virtualizer.isScrolling &&
            hashIndexDelta > 0 &&
            virtualizerContext.virtualizer.scrollOffset == spineIndexOffsetTarget
        ) {
            val newOffset =  virtualizerContext.virtualizer.scrollOffset + hashIndexDelta
            hashIndexDelta = 0
            virtualizerContext.virtualizer.scrollToOffset(
                newOffset,
                jso {
                   align = ScrollAlignment.start
                   behavior = ScrollBehavior.instant
                }
            )
        }
    }
}

external interface EpubSpineItemProps: Props {

    var url: String

    var itemIndex: Int

    var defaultHeight: Height

    var onHeightChanged: (Height) -> Unit

    var onClickLink: (String) -> Unit

    var scrollCommands: Flow<EpubScrollCommand>

    var scrollByFunction: (Int) -> Unit

}

val EpubSpineItem = FC<EpubSpineItemProps> { props ->
    val iframeRef = useRef<HTMLIFrameElement>(null)

    var iframeHeight: Height by useState { props.defaultHeight }

    val loadedCompletable = useMemo(props.url) {
        CompletableDeferred<Unit>()
    }

    useLaunchedEffect(props.scrollCommands, props.itemIndex) {
        props.scrollCommands.filter { it.spineIndex == props.itemIndex }.collect {
            val hash = it.hash ?: return@collect
            loadedCompletable.await()

            val documentEl = iframeRef.current?.contentDocument ?: return@collect

            //find the position of the target
            val elementId = hash.substring(1)
            val targetEl = documentEl.getElementById(elementId)
            if(targetEl != null) {
                val scrollDownBy = targetEl.getBoundingClientRect().top.roundToInt()
                props.scrollByFunction(scrollDownBy)
            }
        }
    }

    iframe {
        src = props.url
        ref = iframeRef

        onLoad = { _ ->
            iframeRef.current?.contentDocument?.body?.also { bodyEl ->
                val loadedHeight = bodyEl.offsetHeight
                val computedStyle = getComputedStyle(bodyEl)
                val calculatedHeight = "calc(${loadedHeight}px + ${computedStyle.marginTop} + ${computedStyle.marginBottom} + 16px)"
                    .unsafeCast<Height>()

                iframeHeight = calculatedHeight
                props.onHeightChanged(calculatedHeight)

                bodyEl.getElementsByTagName("a").forEach { element ->
                    element.addEventListener(
                        type = web.uievents.MouseEvent.Companion.CLICK,
                        callback = { evt ->
                            evt.preventDefault()
                            evt.stopPropagation()
                            val href = element.getAttribute("href")
                            href?.also(props.onClickLink)
                        }
                    )
                }
            }
            loadedCompletable.complete(Unit)
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
        onClickLink = epubViewModel::onClickLink
        scrollToCommands = epubViewModel.epubScrollCommands
        onDismissTableOfContents = epubViewModel::onDismissTableOfContentsDrawer
        onClickTocItem = epubViewModel::onClickTocItem
        onClickToggleTogItem = epubViewModel::onClickToggleTocItem
    }
}

