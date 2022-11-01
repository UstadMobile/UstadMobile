package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.Area
import csstype.Color
import csstype.None
import mui.material.*
import react.dom.html.ReactHTML.nav
import mui.system.sx
import react.FC
import react.Props
import react.router.dom.NavLink
import emotion.react.css
import react.ReactNode

val Sidebar = FC<Props> {
    Box {
        component = nav
        sx {
            gridArea = Area.Sidebar
        }

        Drawer {
            variant = DrawerVariant.permanent
            anchor = DrawerAnchor.left

            Box {
                Toolbar()

                List {
                    NavLink {
                        to = "index.html.old"
                    }

                    css {
                        textDecoration = None.none
                        color = Color.currentcolor
                    }

                    ListItemButton {
                        ListItemText {
                            primary = ReactNode("Home")
                        }
                    }
                }
            }
        }
    }
}