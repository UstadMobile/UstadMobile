package com.ustadmobile.view.components.virtuallist

import react.ReactNode

sealed interface VirtualListElement {

    fun createNode(): ReactNode

}

class VirtualListDataElement<T>(
    private val data: T,
    private val createNodeFn: (T) -> ReactNode,
) : VirtualListElement{

    override fun createNode(): ReactNode {
        return createNodeFn(data)
    }

}

class VirtualListSingleElement(
    private val createNodeFn: () -> ReactNode,
) : VirtualListElement {
    override fun createNode(): ReactNode  = createNodeFn()
}


sealed class VirtualListSection {

    abstract val elements: List<VirtualListElement>

}

class ItemListSection<T>(
    list: List<T>,
    createNode: (T) -> ReactNode
): VirtualListSection() {
    override val elements: List<VirtualListElement> = list.map {
        VirtualListDataElement(it) { data -> createNode(data) }
    }
}

class SingleItemSection(
    private val createNode: () -> ReactNode
) : VirtualListSection() {
    override val elements: List<VirtualListElement>
        get() = listOf(VirtualListSingleElement(createNode))
}


