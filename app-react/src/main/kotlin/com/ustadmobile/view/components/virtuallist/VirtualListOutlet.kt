

package com.ustadmobile.view.components.virtuallist

import csstype.Position
import csstype.pct
import csstype.px
import csstype.translatey
import js.core.jso
import react.*
import react.dom.html.ReactHTML
import web.html.HTMLDivElement

/**
 * VirtualListOutlet creates the full height div (e.g. as per the total height of all virtual items)
 * and creates a div for each current virtual item (which is then positioned accordingly)
 *
 * As per https://tanstack.com/virtual/v3/docs/examples/react/dynamic
 */
val VirtualListOutlet = FC<Props> {
    @Suppress("SpellCheckingInspection")
    val virtualizerContext = useRequiredContext(VirtualListContext)

    ReactHTML.div {
        style = jso {
            height = virtualizerContext.virtualizer.getTotalSize().px
            width = 100.pct
            position = Position.relative
        }

        virtualizerContext.virtualizer.getVirtualItems().forEach { virtualRow ->
            val virtualListElement = virtualizerContext.allRows[virtualRow.index]
            ReactHTML.div {
                key = virtualListElement.key()
                ref = virtualizerContext.virtualizer.measureElement.unsafeCast<Ref<HTMLDivElement>>()
                asDynamic()["data-index"] = virtualRow.index
                style = jso {
                    position = Position.absolute
                    top = 0.px
                    left = 0.px
                    width = 100.pct
                    transform = translatey(virtualRow.start.px)
                }

                + virtualListElement.createNode()
            }
        }
    }
}
