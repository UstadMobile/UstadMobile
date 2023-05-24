package com.ustadmobile.mui.components

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.view.ClazzList2View
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import csstype.Color
import csstype.Display
import csstype.None
import mui.material.*
import mui.icons.material.School
import mui.icons.material.LibraryBooks
import mui.icons.material.Person
import mui.system.sx
import react.FC
import react.Props
import react.router.dom.NavLink
import emotion.react.css
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.nav

external interface SidebarProps: Props {
    var visible: Boolean
}

data class RootScreen(
    val key: String,
    val nameMessageId: Int,
    val icon: FC<*>? = null,
)

val ROOT_SCREENS = listOf(
    RootScreen(ClazzList2View.VIEW_NAME, MessageID.courses, School),
    RootScreen(ContentEntryListViewModel.DEST_NAME, MessageID.library, LibraryBooks),
    RootScreen(PersonListView.VIEW_NAME, MessageID.people, Person)
)

val Sidebar = FC<SidebarProps> { props ->
    val strings = useStringsXml()

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

                    ROOT_SCREENS.forEach { screen ->
                        NavLink {
                            to = screen.key
                            css {
                                textDecoration = None.none
                                color = Color.currentcolor
                            }

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
        }
    }
}