package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.Sizes
import mui.material.Box
import mui.material.DrawerAnchor
import mui.material.List
import mui.material.SwipeableDrawer
import mui.material.Toolbar
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.nav
import web.cssom.Display
import web.cssom.vh

external interface UstadMobileMenuProps: Props {
    var isOpen: Boolean
    var onSetOpen: (Boolean) -> Unit
    var selectedRootItemIndex: Int
}

val UstadMobileMenu = FC<UstadMobileMenuProps> { props ->
    Box {
        component = nav
        sx {
            display = Display.block
        }

        SwipeableDrawer {
            anchor = DrawerAnchor.left
            open = props.isOpen
            onOpen = {
                props.onSetOpen(true)
            }
            onClose = {
                props.onSetOpen(false)
            }

            sx {
                height = 99.vh
            }

            Box {
                Toolbar()

                List {
                    sx {
                        width = Sizes.Sidebar.Width
                    }

                    UstadRootScreenNavLinks {
                        selectedItem = props.selectedRootItemIndex
                        onClick = {
                            props.onSetOpen(false)
                        }
                    }
                }
            }
        }

    }
}