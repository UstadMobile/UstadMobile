package com.ustadmobile

import com.ustadmobile.mui.components.*
import js.core.jso
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createHashRouter
import web.dom.document
import web.html.HTML.div

/**
 * This follows roughly the same design pattern as Kotlin MUI showcase
 * Version #d71c6d1
 */

fun main() {
    val root = document.createElement(div)
        .also { document.body.appendChild(it) }

    createRoot(root)
        .render(App.create())
}


/**
 * Represents MUI specific state e.g. height of appbar (which is required by some screens for height
 * calculations)
 */
data class MuiAppState (
    val appBarHeight: Int = DEFAULT_APPBAR_HEIGHT,
)

private val hashRouter = createHashRouter(
    routes = arrayOf(
        jso {
            path = "/"
            loader = ustadScreensLoader
            shouldRevalidate = {
                false
            }
            Component = UstadScreens
            ErrorBoundary = Error
            children = arrayOf(
                jso {
                    path = ":ustadScreenName"
                    loader = ustadScreenLoader
                    Component = UstadScreen
                    ErrorBoundary = Error
                },
                jso {
                    path = "*"
                    Component = Error
                }
            )
        }
    )
)

private val App = FC<Props> {
    ThemeModule {
        RouterProvider {
            router = hashRouter
        }
    }
}

