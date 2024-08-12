package com.ustadmobile.view.components.virtuallist

import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import js.objects.Object
import js.objects.jso
import react.*
import react.dom.html.ReactHTML.div
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.virtual.useVirtualizer
import web.cssom.ClassName
import web.cssom.scaley
import web.events.EventHandler
import web.events.addEventListener
import web.events.removeEventListener
import web.html.HTMLElement
import web.uievents.WheelEvent


external interface VirtualListProps: PropsWithChildren {
    /**
     * List of sections - should be created using virtualListContent { } block
     */
    var content: List<VirtualListSection>

    var style: CSSProperties?

    /**
     * If true, reverse the order of the list so that the first item will appear at the bottom. This
     * is used on chat (e.g. messages are retrieved starting from the most recent, but this appears
     * as the bottom of the list).
     *
     * This is not explicitly supported by Tanstack Virtualizer, but is handled as per:
     * https://github.com/TanStack/virtual/issues/27
     *
     * Specifically:
     * https://codesandbox.io/p/devbox/immutable-silence-76pwko?file=%2Fsrc%2Fmain.tsx%3A90%2C1-92%2C1
     *
     * This works as "It's transforming the list and items (scaleY -1) and inverting the mousewheel event."
     *
     * This will have the same effect as using reverseLayout on LazyColumn
     * on Jetpack Compose
     */
    var reverseLayout: Boolean?

    var id: String?

    var className: ClassName?
}


/**
 * Create a Virtualized List using Tanstack's virtualizer. The content can be created using a simple
 * builder to mix individual items, fixed size lists (e.g. plain list type), and InfiniteQuery
 * based lists.
 *
 * VirtualList will generate the div that will control the scrolling of the virtual list. Normally
 * this should fill the entire of the viewport (minus the appbar etc). Virtualization can only work
 * when the height is limited, so the virtualizer can determine which items are visible.
 *
 * As per:
 *  As per https://tanstack.com/virtual/v3/docs/examples/react/dynamic
 *
 * The VirtualList uses a combination of state and context to pass the content property
 * to the outlet. This means that changes are delivered ASYNCHRONOUSLY. If the content includes
 * textfields (which require synchronous updates), they must have their own state.
 *
 * See VirtualListPreview for an example of this.
 */
val VirtualList = FC<VirtualListProps> {props ->
    val parentRef = useRef<HTMLElement>(null)

    useEffect(parentRef.current) {
        val handleScroll: EventHandler<WheelEvent, HTMLElement> = EventHandler { evt ->
            evt.preventDefault()
            val currentTarget = evt.currentTarget as? HTMLElement
            if(currentTarget != null){
                currentTarget.scrollTop -= evt.deltaY
            }
        }

        if(props.reverseLayout == true) {
            parentRef.current?.addEventListener(
                WheelEvent.wheel(), handleScroll, jso {
                    passive = false
                }
            )
        }

        cleanup {
            if(props.reverseLayout == true) {
                parentRef.current?.removeEventListener(WheelEvent.wheel(), handleScroll)
            }
        }
    }

    val allRows = useMemo(props.content) {
        props.content.flatMap { section ->
            section.elements
        }
    }


    @Suppress("SpellCheckingInspection")
    val virtualizer = useVirtualizer<HTMLElement, HTMLElement>(jso {
        count =  allRows.size
        getScrollElement= { parentRef.current }
        estimateSize = { 45 }
        overscan = 5
        getItemKey = { index -> allRows[index].key() }
    })

    val virtualizerStateInstance: StateInstance<VirtualListContextData> = useState {
        VirtualListContextData(
            virtualizer = virtualizer,
            allRows = allRows,
            reverseLayout = props.reverseLayout ?: false
        )
    }

    var virtualizerStateVar by virtualizerStateInstance

    useEffect(virtualizer, allRows, props.reverseLayout) {
        virtualizerStateVar = VirtualListContextData(
            virtualizer = virtualizer,
            allRows = allRows,
            reverseLayout = props.reverseLayout ?: false
        )
    }

    div {
        ref = parentRef
        id = props.id
        className = props.className ?: ClassName("VirtualList")

        style = if(props.reverseLayout == true) {
            jso {
                props.style?.also { propsStyle ->
                    Object.assign(this, propsStyle)
                }

                transform = scaley(-1)
            }
        }else {
            props.style
        }

        VirtualListContext(virtualizerStateInstance) {
            + props.children
        }
    }
}

/**
 * Scoped object used to create VirtualList content items from individual items, lists, and
 * infinite queries.
 */
class VirtualListContentScope internal constructor() {

    internal val sections = mutableListOf<VirtualListSection>()

    fun item(key: String? = null, block: () -> ReactNode) {
        sections += SingleItemSection(
            block, key ?: "item-${sections.count { it is SingleItemSection}}"
        )
    }

    fun <T> itemsIndexed(
        list: List<T>,
        key: (item: T, index: Int) -> String,
        createNode: (item: T, index: Int) -> ReactNode,
    ) {
        sections += ItemListSection(list, createNode, key)
    }

    fun <T> items(
        list: List<T>,
        key: (item: T) -> String,
        createNode: (item: T) -> ReactNode,
    ) {
        itemsIndexed(list, {item, _ -> key(item)}) {item, _ -> createNode(item) }
    }

    fun <TItem, TData, TError> infiniteQueryItemsIndexed(
        infiniteQueryResult: UseInfiniteQueryResult<TData, TError>,
        dataPagesToItems: (Array<out TData>) -> List<TItem?>,
        itemToKey: (item: TItem, index: Int) -> String,
        itemToNode: (item: TItem?, index: Int) -> ReactNode,
    ) {
        sections += InfiniteQueryResultSection(
            infiniteQueryResult = infiniteQueryResult,
            infiniteSectionIndex = sections.count { it is InfiniteQueryResultSection<*, *, *> },
            dataPagesToItems = dataPagesToItems,
            itemToKey = { items, index ->
                items[index]?.let { itemToKey(it, index) } ?: "$index"
            },
            createNode = { items, index ->
                itemToNode(items[index], index)
            },
        )
    }

    fun <TItem, TData, TError> infiniteQueryItems(
        infiniteQueryResult: UseInfiniteQueryResult<TData, TError>,
        dataPagesToItems: (Array<out TData>) -> List<TItem?>,
        itemToKey: (item: TItem) -> String,
        itemToNode: (item: TItem?) -> ReactNode,
    ) {
        infiniteQueryItemsIndexed(
            infiniteQueryResult = infiniteQueryResult,
            dataPagesToItems = dataPagesToItems,
            itemToKey = { item, _ -> itemToKey(item) },
            itemToNode = { item, _ -> itemToNode(item) },
        )
    }

    fun <T: Any> infiniteQueryPagingItemsIndexed(
        items: UseInfiniteQueryResult<PagingSourceLoadResult<Int, T>, Throwable>,
        key: (item: T, index: Int) -> String,
        itemToNode: (item: T?, index: Int) -> ReactNode,
    ) {
        sections += InfiniteQueryResultSection(
            infiniteQueryResult = items,
            infiniteSectionIndex = sections.count { it is InfiniteQueryResultSection<*, *, *> },
            dataPagesToItems = { pages ->
                pages.mapNotNull { it as? PagingSourceLoadResultPage<Int, T> }.flatMap {
                    it.data
                }
            },
            itemToKey = { allItems, index ->
                allItems[index]?.let { key(it, index) } ?: "$index"
            },
            createNode = { allItems, index ->
                itemToNode(allItems[index], index)
            }
        )
    }

    fun <T: Any> infiniteQueryPagingItems(
        items: UseInfiniteQueryResult<PagingSourceLoadResult<Int, T>, Throwable>,
        key: (item: T) -> String,
        itemToNode: (item: T?) -> ReactNode
    ) {
        infiniteQueryPagingItemsIndexed(
            items = items,
            key = { item, _ -> key(item) },
            itemToNode = { item, _ -> itemToNode(item) }
        )
    }

    fun <T: Any> infiniteQueryPagingItemsList(
        items: UseInfiniteQueryResult<PagingSourceLoadResult<Int, T>, Throwable>,
        key: (list: List<T?>, index: Int) -> String,
        node: (list: List<T?>, index: Int) -> ReactNode
    ) {
        sections += InfiniteQueryResultSection(
            infiniteQueryResult = items,
            infiniteSectionIndex = sections.count { it is InfiniteQueryResultSection<*, *, *> },
            dataPagesToItems = { pages ->
                pages.mapNotNull { it as? PagingSourceLoadResultPage<Int, T> }.flatMap {
                    it.data
                }
            },
            itemToKey = key,
            createNode = node,
        )
    }

}

/**
 * Create a list of VirtualListSection .
 */
fun virtualListContent(
    block: VirtualListContentScope.() -> Unit
) : List<VirtualListSection> {
    val scope = VirtualListContentScope()
    block(scope)
    return scope.sections
}

