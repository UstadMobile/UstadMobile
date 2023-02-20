package com.ustadmobile.view.components.virtuallist

import react.ReactNode

/**
 * A virtual list element represents an item within a virtual list. This can be an individual
 * element (e.g. just a node creation function), or it can be an element that is part of a list/
 * infinite query where there is an item (e.g. an entity) and a function that generates a node based
 * on the item.
 */
sealed interface VirtualListElement {

    fun createNode(): ReactNode

    fun key(): String

}

class VirtualListSingleElement(
    private val createNodeFn: () -> ReactNode,
    private val key: String,
) : VirtualListElement {
    override fun createNode(): ReactNode  = createNodeFn()

    override fun key() = key
}


/**
 * Element that represents an item that is part of a list of items
 */
class VirtualListItemElement<T>(
    private val item: T,
    private val index: Int,
    private val itemToNode: (item: T, index: Int) -> ReactNode,
    private val itemToKey: (item: T, index: Int) -> String,
) : VirtualListElement{

    override fun key() = itemToKey(item, index)

    override fun createNode(): ReactNode {
        return itemToNode(item, index)
    }

}

class VirtualListInfiniteQueryItemElement<T>(
    private val item: T?,
    private val index: Int,
    private val itemToKey: (item: T, index: Int) -> String,
    private val itemToNode: (item: T?, index: Int) -> ReactNode,

): VirtualListElement {

    override fun key(): String {
        return item?.let { itemToKey(item, index) } ?: "placeholder-$index"
    }

    override fun createNode(): ReactNode = itemToNode(item, index)
}
