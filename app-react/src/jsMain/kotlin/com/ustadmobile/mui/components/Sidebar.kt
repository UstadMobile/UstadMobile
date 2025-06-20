package com.ustadmobile.mui.components

import com.ustadmobile.core.viewmodel.site.COMMON_TOP_LEVEL_NAV_ITEMS
import com.ustadmobile.lib.db.entities.Site
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import dev.icerock.moko.resources.StringResource
import js.objects.jso
import mui.icons.material.Chat
import mui.icons.material.LibraryBooks
import mui.icons.material.Person
import mui.material.Box
import mui.material.Drawer
import mui.material.DrawerAnchor
import mui.material.DrawerVariant
import mui.material.List
import mui.material.Toolbar
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav
import web.cssom.Auto
import web.cssom.Display
import web.cssom.Position
import web.cssom.px
import mui.icons.material.School as SchoolIcon

external interface SidebarProps: Props {
    var visible: Boolean
    var selectedRootItemIndex: Int
    var site: Site?

}

data class RootScreen(
    val key: String,
    val nameMessageId: StringResource,
    val icon: FC<*>? = null,
    val flag: Long
)
val iconMap = mapOf(
    Site.SHOW_COURSE to SchoolIcon,
    Site.SHOW_LIBRARY to LibraryBooks,
    Site.SHOW_MESSAGES to Chat,
    Site.SHOW_PEOPLE to Person
)

val ROOT_SCREENS = COMMON_TOP_LEVEL_NAV_ITEMS.map { info ->
    RootScreen(
        key = info.destRoute,
        nameMessageId = info.label,
        icon = iconMap.getValue(info.flag),
        flag = info.flag
    )
}

val Sidebar = FC<SidebarProps> { props ->
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
                Toolbar {
                    disableGutters = true

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
                            position = Position.absolute
                        }
                    }
                }

                List {
                    sx { width = Sizes.Sidebar.Width }

                    UstadRootScreenNavLinks {
                        selectedItem = props.selectedRootItemIndex
                        idPrefix = "sidebar"
                        currentSite = props.site
                    }
                }

                UstadSidebarBottomBox()
            }
        }
    }
}