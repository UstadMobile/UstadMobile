package com.ustadmobile.view.epubcontent

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useLaunchedEffect
import com.ustadmobile.core.hooks.useOnUnloadEffect
import com.ustadmobile.core.util.ext.forEach
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentUiState
import com.ustadmobile.core.viewmodel.epubcontent.EpubContentViewModel
import com.ustadmobile.core.viewmodel.epubcontent.EpubScrollCommand
import com.ustadmobile.core.viewmodel.epubcontent.EpubTocItem
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.hooks.useWindowFocusedEffect
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListContext
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import js.objects.jso
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
import mui.system.sx
import react.FC
import react.Props
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
import web.dom.getComputedStyle
import web.html.HTMLIFrameElement
import web.scroll.ScrollBehavior
import kotlin.math.roundToInt
import com.ustadmobile.hooks.useWindowSize
import kotlinx.coroutines.delay
import mui.material.Container
import mui.material.DrawerVariant
import mui.system.useMediaQuery
import web.cssom.Auto
import web.cssom.GridTemplateAreas
import web.cssom.array
import web.cssom.ident
import web.dom.scroll
import web.events.Event
import web.events.EventHandler
import web.events.addEventListener
import web.events.removeEventListener
import web.html.HTMLElement

external interface EpubContentProps : Props{
    var uiState: EpubContentUiState

    var onClickLink: (baseUrl: String, href: String) -> Unit

    var scrollToCommands: Flow<EpubScrollCommand>

    var onDismissTableOfContents: () -> Unit

    var onClickTocItem: (EpubTocItem) -> Unit

    var onClickToggleTogItem: (EpubTocItem) -> Unit

    var onSpineIndexChanged: (Int) -> Unit

    var onActiveChanged: (Boolean) -> Unit
}

object EpubArea{
    val NavAreaWidth = 250.px

    val NavArea = ident("nav_area")
    val EpubContentArea = ident("epub_content_area")
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
    val mobileMode = useMediaQuery("(max-width:960px)")


    useWindowFocusedEffect { focused ->
        props.onActiveChanged(focused)
    }

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

    Box {
        sx {
            display = Display.grid
            gridTemplateRows = array(Auto.auto)
            gridTemplateColumns = if(mobileMode){
                array(Auto.auto)
            }else {
                array(Auto.auto, EpubArea.NavAreaWidth)
            }

            gridTemplateAreas = GridTemplateAreas(
                if(mobileMode) {
                    arrayOf(EpubArea.EpubContentArea)
                }else {
                    arrayOf(EpubArea.EpubContentArea, EpubArea.NavArea)
                }
            )
        }

        VirtualList {
            key = "epub_spine_virtual_list"
            style = jso {
                height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
                width = 100.pct
                contain = Contain.strict
                overflowY = Overflow.scroll
                gridArea = EpubArea.EpubContentArea
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

            Container {
                VirtualListOutlet()
            }

            EpubScrollComponent {
                scrollToCommands = props.scrollToCommands
                scrollByCommands = scrollByCommandFlow
                onSpineIndexChanged = props.onSpineIndexChanged
            }
        }

        Drawer {
            key = "epub_drawer"
            sx {
                if(mobileMode)
                    gridArea = EpubArea.NavArea

                width = EpubArea.NavAreaWidth
            }

            anchor = DrawerAnchor.right
            variant = DrawerVariant.temporary
            variant = if(mobileMode) {
                DrawerVariant.temporary
            }else {
                DrawerVariant.permanent
            }

            open = props.uiState.tableOfContentsOpen
            onClose = { _, _ ->
                props.onDismissTableOfContents()
            }

            EpubTocListComponent {
                +props
            }
        }
    }
}

external interface EpubScrollProps : Props {
    var scrollToCommands: Flow<EpubScrollCommand>

    var scrollByCommands: Flow<Int>

    var onSpineIndexChanged: (Int) -> Unit
}

/*
 * EpubScrollComponent has to be a child component of VirtualList in order to use VirtualListContext
 */
val EpubScrollComponent = FC<EpubScrollProps> { props ->

    val virtualizerContext by useRequiredContext(VirtualListContext)
    val scrollElement = virtualizerContext.virtualizer.scrollElement

    useEffect(scrollElement) {
        val scrollListener: (Event) -> Unit = {
            val scrollTop = scrollElement?.scrollTop
            if(scrollTop != null){
                val spineIndex = virtualizerContext.virtualizer
                    .getVirtualItemForOffset(scrollTop.roundToInt()).index
                props.onSpineIndexChanged(spineIndex)
            }
        }

        scrollElement?.addEventListener(Event.Companion.scroll(), scrollListener)

        cleanup {
            scrollElement?.removeEventListener(Event.Companion.scroll(), scrollListener)
        }
    }

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
            virtualizerContext.virtualizer.scrollToOffset(
                offsetAsInt,
                jso {
                    align = ScrollAlignment.start
                    behavior = ScrollBehavior.instant
                }
            )
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

    /* Update the height of the iframe element to match the height of the content. Scrolling is
     * handled by the virtual list, we want to avoid scrollbars showing within the iframe e.g.
     * the iframe height should match the body height of its contents plus margins. 16px added to
     * allow some space between pages. This needs to be set once the content has loaded and again
     * if the window size changes.
     */
    fun updateIframeHeight() {
        val bodyEl = iframeRef.current?.contentDocument?.body ?: return
        val loadedHeight = bodyEl.offsetHeight
        val computedStyle = getComputedStyle(bodyEl)
        val calculatedHeight = "calc(${loadedHeight}px + ${computedStyle.marginTop} + ${computedStyle.marginBottom} + 16px)"
            .unsafeCast<Height>()

        iframeHeight = calculatedHeight
        props.onHeightChanged(calculatedHeight)
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

    /*
     * If the width of the window changes, this will very likely change the height of the contents
     * (e.g. if width narrows it gets taller). If the window height changes this may affect height
     * inside iframes sometimes (e.g. where they use vh measurement units).
     */
    val windowSize = useWindowSize()

    useLaunchedEffect(windowSize.width, windowSize.height) {
        //Needed to allow content within the frame to settle. Not ideal, but works for now.
        delay(300)
        if(loadedCompletable.isCompleted) {
            updateIframeHeight()
        }
    }

    iframe {
        src = props.url
        ref = iframeRef

        onLoad = { _ ->
            iframeRef.current?.contentDocument?.body?.also { bodyEl ->
                updateIframeHeight()

                bodyEl.getElementsByTagName("a").forEach { element ->
                    element.addEventListener(
                        type = web.uievents.MouseEvent.click<HTMLElement>(),
                        handler = EventHandler { evt ->
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

    useOnUnloadEffect(epubViewModel::onUnload)

    EpubContentComponent {
        uiState = uiStateVal
        onClickLink = epubViewModel::onClickLink
        scrollToCommands = epubViewModel.epubScrollCommands
        onDismissTableOfContents = epubViewModel::onDismissTableOfContentsDrawer
        onClickTocItem = epubViewModel::onClickTocItem
        onClickToggleTogItem = epubViewModel::onClickToggleTocItem
        onSpineIndexChanged = epubViewModel::onSpineIndexChanged
        onActiveChanged = epubViewModel::onActiveChanged
    }
}

