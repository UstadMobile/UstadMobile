package com.ustadmobile.view.components.virtuallist

import com.ustadmobile.view.UstadScreenProps
import csstype.*
import js.core.jso
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemText
import mui.material.Typography
import react.*

val VirtualListPreview = FC<UstadScreenProps> {props ->
    VirtualList {
        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = csstype.Contain.strict
            overflowY = csstype.Overflow.scroll
        }


        content = virtualListContent {
            item {
                Typography.create {
                    +"List Header "
                }
            }

            items(
                list = (0..100).toList()
            ) { number ->
                ListItem.create {
                    ListItemText {
                        + "item $number"
                    }
                }
            }
        }
    }
}
