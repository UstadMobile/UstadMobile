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

}

class VirtualListSingleElement(
    private val createNodeFn: () -> ReactNode,
) : VirtualListElement {
    override fun createNode(): ReactNode  = createNodeFn()
}


class VirtualListItemElement<T>(
    private val item: T,
    private val itemToNode: (T) -> ReactNode,
) : VirtualListElement{

    override fun createNode(): ReactNode {
        return itemToNode(item)
    }

}

class VirtualListInfiniteQueryItemElement<T>(
    private val item: T?,
    private val itemToNode: (T?) -> ReactNode,
): VirtualListElement {
    override fun createNode(): ReactNode = itemToNode(item)
}
