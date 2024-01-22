package com.ustadmobile.view.components.virtuallist

import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import js.core.Object
import js.core.jso
import react.*
import react.dom.html.ReactHTML.div
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.virtual.useVirtualizer
import web.cssom.scaley
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
 * See VirtualListPreview for an example of this.
 */
val VirtualList = FC<VirtualListProps> {props ->
    val parentRef = useRef<HTMLElement>(null)

    useEffect(parentRef.current) {
        val handleScroll: (evt: WheelEvent) -> Unit = { evt ->
            evt.preventDefault()
            val currentTarget = evt.currentTarget as? HTMLElement
            if(currentTarget != null){
                currentTarget.scrollTop -= evt.deltaY
            }
        }

        if(props.reverseLayout == true) {
            parentRef.current?.addEventListener(
                WheelEvent.WHEEL, handleScroll, jso {
                    passive = false
                }
            )
        }

        cleanup {
            if(props.reverseLayout == true) {
                parentRef.current?.removeEventListener(WheelEvent.WHEEL, handleScroll)
            }
        }
    }

    div {
        ref = parentRef
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

        VirtualListContext(
            VirtualListContextData(
                virtualizer = virtualizer,
                allRows = allRows,
                reverseLayout = props.reverseLayout ?: false
            )
        ) {
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
            itemToKey = itemToKey,
            createNode = itemToNode,
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
            itemToKey = key,
            createNode = itemToNode
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

