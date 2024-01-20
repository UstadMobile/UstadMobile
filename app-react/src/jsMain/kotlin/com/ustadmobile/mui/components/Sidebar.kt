package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import dev.icerock.moko.resources.StringResource
import mui.icons.material.Chat
import web.cssom.Display
import mui.material.Box
import mui.material.Drawer
import mui.material.DrawerVariant
import mui.material.DrawerAnchor
import mui.material.Toolbar
import mui.material.List
import mui.icons.material.School as SchoolIcon
import mui.icons.material.LibraryBooks
import mui.icons.material.Person
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML.nav

external interface SidebarProps: Props {
    var visible: Boolean
    var selectedRootItemIndex: Int
}

data class RootScreen(
    val key: String,
    val nameMessageId: StringResource,
    val icon: FC<*>? = null,
)

val ROOT_SCREENS = listOf(
    RootScreen(ClazzListViewModel.DEST_NAME_HOME, MR.strings.courses, SchoolIcon),
    RootScreen(ContentEntryListViewModel.DEST_NAME_HOME, MR.strings.library, LibraryBooks),
    RootScreen(ConversationListViewModel.DEST_NAME_HOME, MR.strings.messages, Chat),
    RootScreen(PersonListViewModel.DEST_NAME_HOME, MR.strings.people, Person)
)

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
                Toolbar()

                List {
                    sx { width = Sizes.Sidebar.Width }

                    UstadRootScreenNavLinks {
                        selectedItem = props.selectedRootItemIndex
                    }
                }
            }
        }
    }
}