package com.ustadmobile.mui.components

import com.ustadmobile.entities.USTAD_SCREENS
import com.ustadmobile.hooks.useMuiAppState
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.px
import js.core.tupleOf
import mui.lab.TabPanel
import mui.material.Box
import mui.system.sx
import react.*
import web.url.URLSearchParams

val TabSearchParamContext = createContext<URLSearchParams>()


external interface UstadScreenTabProps: PropsWithChildren {

    var args: Map<String, String>

    var viewName: String

    var value: String

}

fun Map<String, String>.toURLSearchParams():  URLSearchParams {
    return URLSearchParams(
        entries.map { tupleOf(it.key, it.value) }.toTypedArray()
    )
}

val UstadScreenTabPanel = FC<UstadScreenTabProps> {props ->

    val searchParams = useMemo(props.args) {
        props.args.toURLSearchParams()
    }

    val tabState : UstadScreenTabsState by useRequiredContext(UstadScreenTabsStateContext)
    val muiAppState = useMuiAppState()

    //TabSearchParamContext will override the UrlSearchParams used by useUstadViewModel
    TabSearchParamContext(searchParams) {
        TabPanel {
            value = props.value

            //Any margin must be handled by the tab itself
            sx {
                padding = 0.px
                margin = 0.px
            }

            Box {
                sx {
                    height = "calc(100vh - ${muiAppState.appBarHeight + tabState.height}px)".unsafeCast<Height>()
                    overflowY = "auto".unsafeCast<Overflow>()
                }

                try {
                    + USTAD_SCREENS.single { it.key == props.viewName }.component.create()
                }catch(e: Exception) {
                    throw IllegalArgumentException("Cannot find/error creating view for ${props.viewName}")
                }

            }
        }
    }
}

