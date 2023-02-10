package com.ustadmobile.view

import com.ustadmobile.core.components.NavHost
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.appstate.Snack
import com.ustadmobile.mui.common.Area
import csstype.Overflow
import csstype.pct
import csstype.px
import csstype.vh
import mui.material.Snackbar
import mui.material.Typography
import mui.system.Box
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.main
import react.router.Outlet
import react.router.Route
import react.router.Routes
import web.html.HTMLElement

private val DEFAULT_PADDING = 30.px

typealias ShowSnackFunction = (Snack) -> Unit

external interface UstadScreenProps: Props {

    var onAppUiStateChanged: (AppUiState) -> Unit

    var onShowSnackBar: ShowSnackFunction?

    var parentRef: RefObject<HTMLElement>

}

external interface ContentProps: Props {

    var onAppUiStateChanged: (AppUiState) -> Unit

}

val Content = FC<ContentProps> { props ->

    val contentParentRef = useRef<HTMLElement>(null)

    val showcases = useContext(UstadScreensContext)

    var snack: Snack? by useState { null }

    val showSnackFunction: ShowSnackFunction = {
        snack = it
    }

    Snackbar {
        open = snack != null
        onClose = { evt, closeReason ->
            snack = null
        }

        Typography {
            + (snack?.message ?: "")
        }
    }

    Routes {
        Route {
            path = "/"
            element = Box.create {
                ref = contentParentRef

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
                        asDynamic().onShowSnackBar = showSnackFunction
                        asDynamic().parentRef = contentParentRef
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
