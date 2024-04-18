

package com.ustadmobile.view.components.virtuallist

import web.cssom.Position
import web.cssom.pct
import web.cssom.px
import web.cssom.translatey
import js.objects.jso
import react.*
import react.dom.html.ReactHTML
import web.cssom.TransformFunction
import web.html.HTMLDivElement

/**
 * VirtualListOutlet creates the full height div (e.g. as per the total height of all virtual items)
 * and creates a div for each current virtual item (which is then positioned accordingly)
 *
 * As per https://tanstack.com/virtual/v3/docs/examples/react/dynamic
 */
val VirtualListOutlet = FC<Props> {
    @Suppress("SpellCheckingInspection")
    val virtualizerContext by useRequiredContext(VirtualListContext)

    ReactHTML.div {
        style = jso {
            height = virtualizerContext.virtualizer.getTotalSize().px
            width = 100.pct
            position = Position.relative
        }

        virtualizerContext.virtualizer.getVirtualItems().forEachIndexed { _, virtualRow ->
            val virtualListElement = virtualizerContext.allRows
                .getOrNull(virtualRow.index)

            virtualListElement?.also { virtualListEl ->
                ReactHTML.div {
                    key = virtualListEl.key()
                    ref = virtualizerContext.virtualizer.measureElement.unsafeCast<Ref<HTMLDivElement>>()
                    asDynamic()["data-index"] = virtualRow.index
                    style = jso {
                        position = Position.absolute
                        top = 0.px
                        left = 0.px
                        width = 100.pct
                        transform = if(virtualizerContext.reverseLayout) {
                            "translatey(${virtualRow.start.px}) scaley(-1)".unsafeCast<TransformFunction>()
                        }else {
                            translatey(virtualRow.start.px)
                        }
                    }

                    + virtualListEl.createNode()
                }
            }
        }
    }
}
