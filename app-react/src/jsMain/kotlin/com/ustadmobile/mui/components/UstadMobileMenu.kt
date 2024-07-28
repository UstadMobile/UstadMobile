package com.ustadmobile.mui.components

import com.ustadmobile.mui.common.Sizes
import js.objects.jso
import mui.material.Box
import mui.material.DrawerAnchor
import mui.material.List
import mui.material.SwipeableDrawer
import mui.material.Toolbar
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav
import web.cssom.Auto
import web.cssom.Display
import web.cssom.px
import web.cssom.vh

external interface UstadMobileMenuProps: Props {
    var isOpen: Boolean
    var onSetOpen: (Boolean) -> Unit
    var selectedRootItemIndex: Int
    var visible: Boolean
}

val UstadMobileMenu = FC<UstadMobileMenuProps> { props ->
    Box {
        component = nav
        sx {
            display = if(props.visible) {
                Display.block
            }else {
                "none".unsafeCast<Display>()
            }
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

                    img {
                        id = "top_center_brand_img"
                        src = "assets/top-start.svg"
                        alt = ""
                        style = jso {
                            display = Display.block
                            paddingTop = 8.px
                            paddingBottom = 8.px
                            paddingLeft = 16.px
                            paddingRight = 16.px
                            marginLeft = Auto.auto
                            marginRight = Auto.auto
                            maxWidth = Sizes.Sidebar.Width
                            maxHeight = Sizes.Header.Height
                        }
                    }

                    UstadRootScreenNavLinks {
                        selectedItem = props.selectedRootItemIndex
                        idPrefix = "mobilemenu"
                        onClick = {
                            props.onSetOpen(false)
                        }
                    }
                }
            }

            UstadSidebarBottomBox()
        }

    }
}