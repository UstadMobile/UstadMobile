package com.ustadmobile.view

import com.ustadmobile.core.components.NavHost
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.mui.common.Area
import csstype.px
import mui.material.Typography
import mui.system.Box
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.main
import react.router.Outlet
import react.router.Route
import react.router.Routes
import react.useContext

private val DEFAULT_PADDING = 30.px

external interface UstadScreenProps: Props {

    var onAppUiStateChanged: (AppUiState) -> Unit

}

val Content = FC<UstadScreenProps> { props ->
    val showcases = useContext(UstadScreensContext)

    Routes {
        Route {
            path = "/"
            element = Box.create {
                component = main
                sx {
                    gridArea = Area.Content
                    padding = DEFAULT_PADDING
                }

                NavHost {
                    Outlet()
                }
            }

            showcases.forEachIndexed { i, (key, _, Component) ->
                Route {
                    index = i == 0
                    path = key
                    element = Component.create {
                        asDynamic().onAppUiStateChanged = props.onAppUiStateChanged
                    }
                }
            }

            Route {
                path = "*"
                element = Typography.create { +"404 Page Not Found" }
            }
        }
    }
}
