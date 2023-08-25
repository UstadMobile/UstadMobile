package com.ustadmobile.view.components.virtuallist

import com.ustadmobile.door.paging.LoadResult
import web.cssom.*
import js.core.jso
import react.*
import react.dom.html.ReactHTML.div
import tanstack.react.query.UseInfiniteQueryResult
import tanstack.react.virtual.useVirtualizer
import web.html.HTMLElement


external interface VirtualListProps: PropsWithChildren {
    /**
     * List of sections - should be created using virtualListContent { } block
     */
    var content: List<VirtualListSection>

    var style: CSSProperties?
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

    div {
        ref = parentRef
        style = props.style


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

        VirtualListContext(VirtualListContextData(virtualizer, allRows)) {
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
        items: UseInfiniteQueryResult<LoadResult<Int, T>, Throwable>,
        key: (item: T, index: Int) -> String,
        itemToNode: (item: T?, index: Int) -> ReactNode,
    ) {
        sections += InfiniteQueryResultSection(
            infiniteQueryResult = items,
            infiniteSectionIndex = sections.count { it is InfiniteQueryResultSection<*, *, *> },
            dataPagesToItems = { pages ->
                console.log("Mapping data pages to items")
                pages.mapNotNull { it as? LoadResult.Page<Int, T> }.flatMap {
                    it.data
                }
            },
            itemToKey = key,
            createNode = itemToNode
        )
    }

    fun <T: Any> infiniteQueryPagingItems(
        items: UseInfiniteQueryResult<LoadResult<Int, T>, Throwable>,
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

