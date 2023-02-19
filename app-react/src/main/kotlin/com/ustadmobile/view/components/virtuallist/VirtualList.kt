package com.ustadmobile.view.components.virtuallist

import csstype.*
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

        val allRows = props.content.flatMap { section ->
            section.elements
        }

        @Suppress("SpellCheckingInspection")
        val virtualizer = useVirtualizer<HTMLElement, HTMLElement>(jso {
            count =  allRows.size
            getScrollElement= { parentRef.current }
            estimateSize = { 45 }
            overscan = 5
            getItemKey = { index -> "$index" }
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

    fun item(block: () -> ReactNode) {
        sections += SingleItemSection(block)
    }

    fun <T> items(
        list: List<T>,
        createNode: (T) -> ReactNode
    ) {
        sections += ItemListSection(list, createNode)
    }

    fun <TItem, TData, TError> infiniteQueryItems(
        infiniteQueryResult: UseInfiniteQueryResult<TData, TError>,
        dataPageToItems: (TData) -> List<TItem?>,
        itemToNode: (TItem?) -> ReactNode,
    ) {
        sections += InfiniteQueryResultSection(infiniteQueryResult, dataPageToItems, itemToNode)
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

