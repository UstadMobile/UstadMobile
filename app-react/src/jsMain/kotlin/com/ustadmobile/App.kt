package com.ustadmobile

import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.mui.components.*
import com.ustadmobile.view.redirect.RedirectScreen
import com.ustadmobile.wrappers.jsjodatime.jsJodaTz
import js.objects.jso
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import react.router.dom.createHashRouter
import web.dom.document
import web.html.HTML.div
import kotlinext.js.require


/**
 * This follows roughly the same design pattern as Kotlin MUI showcase
 * Version #d71c6d1
 */

fun main() {
    //As per Quill wrapper (and demo). Css loader must be enabled.
    require<dynamic>("react-quill/dist/quill.snow.css")

    // As per https://github.com/Kotlin/kotlinx-datetime/issues/178
    //Avoid joda time timezones being DCE'd out
    jsJodaTz

    val root = document.createElement(div)
        .also { document.body.appendChild(it) }

    createRoot(root)
        .render(App.create())
}


/**
 * Represents MUI specific state e.g. height of appbar (which is required by some screens for height
 * calculations).
 */
data class MuiAppState (
    val appBarHeight: Int = Sizes.Header.HeightInPx,
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
                    Component = RedirectScreen
                    index = true
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

