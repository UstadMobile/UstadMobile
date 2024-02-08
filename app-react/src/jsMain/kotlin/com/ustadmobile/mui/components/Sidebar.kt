package com.ustadmobile.mui.components

import com.ustadmobile.core.MR
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.domain.getversion.GetVersionUseCase
import com.ustadmobile.core.domain.showpoweredby.GetShowPoweredByUseCase
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.core.viewmodel.person.list.PersonListViewModel
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import dev.icerock.moko.resources.StringResource
import js.core.jso
import web.cssom.Display
import mui.material.*
import mui.icons.material.School
import mui.icons.material.LibraryBooks
import mui.icons.material.Person
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.kodein.di.direct
import org.kodein.di.instance
import react.FC
import react.Props
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.nav
import react.useMemo
import react.useRequiredContext
import web.cssom.Position
import web.cssom.px

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
    RootScreen(ClazzListViewModel.DEST_NAME_HOME, MR.strings.courses, School),
    RootScreen(ContentEntryListViewModel.DEST_NAME_HOME, MR.strings.library, LibraryBooks),
    RootScreen(PersonListViewModel.DEST_NAME_HOME, MR.strings.people, Person)
)

val Sidebar = FC<SidebarProps> { props ->
    val di = useRequiredContext(DIContext)
    val strings = useStringProvider()

    val version = useMemo(dependencies = emptyArray()) {
        di.direct.instance<GetVersionUseCase>().invoke().versionString
    }
    val showPoweredBy = useMemo(dependencies = emptyArray()) {
        di.direct.instance<GetShowPoweredByUseCase>().invoke()
    }

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

                img {
                    id = "top_center_brand_img"
                    src = "assets/top-start.svg"
                    alt = ""
                    style = jso  {
                        padding = 16.px
                        maxWidth = Sizes.Sidebar.Width
                    }
                }

                List {
                    sx { width = Sizes.Sidebar.Width }

                    UstadRootScreenNavLinks {
                        selectedItem = props.selectedRootItemIndex
                    }
                }

                Box {
                    id = "drawer_version_info"
                    sx {
                        position = Position.absolute
                        bottom = 0.px
                        padding = 16.px
                    }

                    Typography {
                        align = TypographyAlign.center
                        variant = TypographyVariant.caption
                        + "${strings[MR.strings.version]} $version"
                    }

                    br()

                    if(showPoweredBy) {
                        UstadPoweredByLink()
                    }
                }
            }
        }
    }
}