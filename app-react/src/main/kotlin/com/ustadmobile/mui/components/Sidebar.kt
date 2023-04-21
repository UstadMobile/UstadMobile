package com.ustadmobile.mui.components

import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import csstype.Color
import csstype.Display
import csstype.None
import mui.material.*
import mui.system.sx
import react.FC
import react.Props
import react.router.dom.NavLink
import emotion.react.css
import react.ReactNode
import react.dom.html.ReactHTML.nav

external interface SidebarProps: Props {
    var visible: Boolean
}

val Sidebar = FC<SidebarProps> { props ->
    val ustadScreens = USTAD_SCREENS
    Box {
        component = nav
        sx {
            gridArea = Area.Sidebar
            if(props.visible) {
                display = Display.block
            }else {
                asDynamic().display = "none"
            }
        }

        Drawer {
            variant = DrawerVariant.permanent
            anchor = DrawerAnchor.left

            Box {
                Toolbar()


                List {
                    sx { width = Sizes.Sidebar.Width }

                    for((key, name) in ustadScreens) {
                        NavLink {
                            to = key

                            css {
                                textDecoration = None.none
                                color = Color.currentcolor
                            }

                            ListItemButton {
                                ListItemText {
                                    primary = ReactNode(name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}