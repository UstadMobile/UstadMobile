package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.appstate.TabItem
import com.ustadmobile.entities.UstadScreens
import web.cssom.Position
import web.cssom.pct
import web.cssom.px
import mui.lab.TabContext
import mui.material.Box
import mui.material.Tab
import mui.material.Tabs
import mui.system.sx
import react.*
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.json
import kotlinx.browser.window
import web.cssom.None

data class UstadScreenTabsState(val height: Int = DEFAULT_HEIGHT) {
    companion object {
        const val DEFAULT_HEIGHT = 48
    }
}

typealias TabSizeStateInstance = StateInstance<UstadScreenTabsState>

val UstadScreenTabsStateContext = createContext<TabSizeStateInstance>()


/**
 * UstadScreenTabs will take a list of TabItem (each one specifying a screen name, argument map, and
 * label string) and create MUI tabs for the given list.
 *
 * The active tab will be remembered in the history state.
 */
external interface UstadScreenTabsProps: Props {

    var tabs: List<TabItem>

    /**
     * The screens that can be used within the component. This should be specified by the parent
     * component, so that Screen components which are used only within tabs cannot be accessed
     * directly by browser url.
     */
    var screens: UstadScreens?

    var autoHideIfOneTab: Boolean?

}

private const val STATE_ACTIVE_TAB_KEY = "activeTab"

val UstadScreenTabs = FC<UstadScreenTabsProps> { props ->

    val history = useMemo {
        window.history
    }

    var currentTab by useState {
        window.history.state?.unsafeCast<Json>()?.get(STATE_ACTIVE_TAB_KEY) as? String ?: "0"
    }

    val theme by useRequiredContext(ThemeContext)
    val tabSizeState = useState { UstadScreenTabsState() }
    var tabSizeStateVar by tabSizeState

    val tabsRef = useRef<HTMLElement>(null)

    useEffect(tabsRef.current?.clientHeight) {
        val currentTabHeight = tabsRef.current?.clientHeight ?: UstadScreenTabsState.DEFAULT_HEIGHT
        if(tabSizeState.component1().height != currentTabHeight) {
            tabSizeStateVar = tabSizeStateVar.copy(
                height = currentTabHeight
            )
        }
    }

    UstadScreenTabsStateContext(tabSizeState) {
        Box {
            sx {
                width = 100.pct
            }

            TabContext {
                value = currentTab


                Box {
                    sx {
                        borderBottom = 1.px
                        borderColor = theme.palette.divider
                        if(props.tabs.size == 1 && props.autoHideIfOneTab == true){
                            display = None.none
                        }
                    }

                    Tabs {
                        ref = tabsRef

                        sx {
                            position = Position.sticky
                        }

                        value = currentTab

                        onChange = { evt, newTab ->
                            currentTab = newTab
                            val newState = (window.history.state?.unsafeCast<Json>() ?: json()).also {
                                it[STATE_ACTIVE_TAB_KEY] = newTab
                            }
                            history.replaceState(newState, "")
                        }

                        props.tabs.forEachIndexed {  index, tab ->
                            Tab {
                                label = ReactNode(tab.label)
                                value = index.toString()
                            }
                        }
                    }
                }

                props.tabs.forEachIndexed { index, tab ->
                    UstadScreenTabPanel {
                        value = index.toString()
                        viewName = tab.viewName
                        args = tab.args
                        screens = props.screens
                    }
                }
            }
        }
    }


}