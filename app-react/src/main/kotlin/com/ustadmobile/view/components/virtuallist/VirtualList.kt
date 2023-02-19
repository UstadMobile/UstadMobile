package com.ustadmobile.view.components.virtuallist

import csstype.*
import js.core.jso
import mui.material.Container
import react.*
import react.dom.html.ReactHTML.div
import tanstack.react.virtual.useVirtualizer
import web.html.HTMLDivElement
import web.html.HTMLElement


external interface VirtualListProps: Props {
    var content: List<VirtualListSection>

    var style: CSSProperties?
}

data class VirtualListContextData(
    val parentRef: RefObject<HTMLElement>
)

val VirtualListContext = createContext<VirtualListContextData>()

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


        Container {
            div {
                style = jso {
                    height = virtualizer.getTotalSize().px
                    width = 100.pct
                    position = Position.relative
                }


                virtualizer.getVirtualItems().forEach { virtualRow ->
                    div {
                        key = "${virtualRow.index}"
                        ref = virtualizer.measureElement.unsafeCast<Ref<HTMLDivElement>>()
                        asDynamic()["data-index"] = virtualRow.index
                        style = jso {
                            position = Position.absolute
                            top = 0.px
                            left = 0.px
                            width = 100.pct
                            height = virtualRow.size.px
                            transform = translatey(virtualRow.start.px)
                        }

                        + allRows[virtualRow.index].createNode()
                    }
                }
            }
        }
    }
}



class VirtualListContentScope {

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

}

fun virtualListContent(
    block: VirtualListContentScope.() -> Unit
) : List<VirtualListSection> {
    val scope = VirtualListContentScope()
    block(scope)
    return scope.sections
}

