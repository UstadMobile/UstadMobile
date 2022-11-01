package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.view.UstadScreensContext
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
import react.useContext

val Sidebar = FC<Props> {
    val ustadScreens = useContext(UstadScreensContext)
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