package com.ustadmobile.mui.components

import com.ustadmobile.core.hooks.useStringProvider
import emotion.react.css
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.dom.NavLink
import web.cssom.Color
import web.cssom.None

external interface UstadRootScreenNavLinksProps: Props {
    var selectedItem: Int

    var onClick: (() -> Unit)?

    var idPrefix: String
}

val UstadRootScreenNavLinks = FC<UstadRootScreenNavLinksProps> { props ->
    val strings = useStringProvider()
    ROOT_SCREENS.forEachIndexed { index, screen ->
        NavLink {
            to = screen.key
            id = "${props.idPrefix}_${screen.key}"

            props.onClick?.also { onClickFn ->
                onClick = {
                    onClickFn()
                }
            }

            css {
                textDecoration = None.none
                color = Color.currentcolor
            }

            ListItem {
                selected = index == props.selectedItem

                ListItemButton {
                    ListItemIcon {
                        + screen.icon?.create()
                    }

                    ListItemText {
                        primary = ReactNode(strings[screen.nameMessageId])
                    }
                }
            }
        }
    }
}