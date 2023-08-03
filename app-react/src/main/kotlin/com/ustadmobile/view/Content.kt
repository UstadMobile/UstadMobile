package com.ustadmobile.view

import com.ustadmobile.core.components.NavHost
import com.ustadmobile.mui.common.Area
import com.ustadmobile.mui.components.NavResultReturnerModule
import csstype.px
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.router.Outlet

val Content = FC<Props> {
    Box {
        component = ReactHTML.main
        sx {
            gridArea = Area.Content
            padding = 0.px
        }

        NavResultReturnerModule {
            NavHost {
                Outlet()
            }
        }
    }
}
