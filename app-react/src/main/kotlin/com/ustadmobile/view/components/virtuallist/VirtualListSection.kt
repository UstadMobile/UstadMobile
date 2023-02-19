package com.ustadmobile.view.components.virtuallist

import react.ReactNode
import react.create
import tanstack.react.query.UseInfiniteQueryResult


/**
 * A VirtualListSection represents one "section" of the list e.g. one individual ReactNode, or
 * a list of nodes to be generated from a fixed list / infinite query.
 */
sealed class VirtualListSection {

    abstract val elements: List<VirtualListElement>

}

class ItemListSection<T>(
    list: List<T>,
    createNode: (T) -> ReactNode
): VirtualListSection() {
    override val elements: List<VirtualListElement> = list.map {
        VirtualListItemElement(it) { data -> createNode(data) }
    }
}

class SingleItemSection(
    createNode: () -> ReactNode
) : VirtualListSection() {
    override val elements: List<VirtualListElement> = listOf(VirtualListSingleElement(createNode))
}

class InfiniteQueryResultSection<TItem, TData, TError>(
    private val infiniteQueryResult: UseInfiniteQueryResult<TData, TError>,
    private val dataPageToItems: (TData) -> List<TItem?>,
    private val createNode: (TItem?) -> ReactNode,
): VirtualListSection() {

    override val elements: List<VirtualListElement>
        get() {
            val resultRows: List<TItem?> = infiniteQueryResult.data?.pages?.flatMap { data: TData ->
                dataPageToItems(data)
            } ?: listOf()

            val queryResult = infiniteQueryResult
            return resultRows.map {
                VirtualListInfiniteQueryItemElement(it) { item ->
                    InfiniteQueryItemHolder.create {
                        this.infiniteQueryResult = queryResult
                        this.loadedItems = resultRows
                        this.item = item

                        + createNode(item)
                    }
                }
            }
        }
}
