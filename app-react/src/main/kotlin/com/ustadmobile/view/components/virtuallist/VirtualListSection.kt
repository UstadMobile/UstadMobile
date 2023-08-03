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
    createNode: (item: T, index: Int) -> ReactNode,
    itemToKey: (item: T, index: Int) -> String,
): VirtualListSection() {
    override val elements: List<VirtualListElement> = list.mapIndexed { index, item ->
        VirtualListItemElement(
            item = item,
            index = index,
            itemToKey = itemToKey,
            itemToNode = createNode,
        )
    }
}

class SingleItemSection(
    createNode: () -> ReactNode,
    key: String,
) : VirtualListSection() {
    override val elements: List<VirtualListElement> = listOf(VirtualListSingleElement(createNode, key))
}

class InfiniteQueryResultSection<TItem, TData, TError>(
    private val infiniteQueryResult: UseInfiniteQueryResult<TData, TError>,
    private val infiniteSectionIndex: Int,
    private val dataPagesToItems: (Array<out TData>) -> List<TItem?>,
    private val itemToKey: (item: TItem, index: Int) -> String,
    private val createNode: (item: TItem?, index: Int) -> ReactNode,
): VirtualListSection() {

    override val elements: List<VirtualListElement>
        get() {
            val resultRows = infiniteQueryResult.data?.pages?.let {
                dataPagesToItems(it)
            } ?: listOf()

            val queryResult = infiniteQueryResult
            val itemToKeyFn = { keyItem: TItem?, keyIndex: Int ->
                keyItem?.let { itemToKey(it, keyIndex) } ?: "placeholder_${infiniteSectionIndex}_$keyIndex"
            }

            return resultRows.mapIndexed { index, item ->
                VirtualListInfiniteQueryItemElement(
                    item = item,
                    index = index,
                    itemToKey = itemToKeyFn,
                ) { nodeItem, _ ->
                    InfiniteQueryItemHolder.create {
                        this.infiniteQueryResult = queryResult
                        this.loadedItems = resultRows
                        this.item = nodeItem

                        + createNode(nodeItem, index)
                    }
                }
            }
        }
}
