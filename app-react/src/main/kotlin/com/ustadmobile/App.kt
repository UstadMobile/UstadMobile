package com.ustadmobile

import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.mui.components.Header
import com.ustadmobile.mui.components.Sidebar
import com.ustadmobile.mui.components.ThemeModule
import com.ustadmobile.view.Content
import com.ustadmobile.view.UstadScreensModule
import csstype.Auto.auto
import csstype.Display
import csstype.GridTemplateAreas
import csstype.array
import kotlinx.browser.document
import mui.material.useMediaQuery
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.dom.HashRouter

fun main() {
    createRoot(document.createElement("div").also { document.body!!.appendChild(it) })
        .render(App.create())
}

private val App = FC<Props> {
    val mobileMode = false//useMediaQuery("(max-width:960px)")

    HashRouter {
        UstadScreensModule {
            ThemeModule {
                Box {
                    sx {
                        display = Display.grid
                        gridTemplateRows = array(
                            Sizes.Header.Height,
                            auto,
                        )
                        gridTemplateColumns = array(
                            Sizes.Sidebar.Width, auto,
                        )
                        gridTemplateAreas = GridTemplateAreas(
                            arrayOf(Area.Header, Area.Header),
                            if (mobileMode)
                                arrayOf(Area.Content, Area.Content)
                            else
                                arrayOf(Area.Sidebar, Area.Content),
                        )
                    }

                    Header()
                    //if (mobileMode) Menu() else Sidebar()
                    Sidebar()
                    Content()
                }
            }
        }
    }
}
