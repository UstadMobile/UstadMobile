package com.ustadmobile.mui.components

import com.ustadmobile.core.impl.appstate.TabItem
import csstype.Position
import csstype.pct
import csstype.px
import mui.lab.TabContext
import mui.material.Box
import mui.material.Tab
import mui.material.Tabs
import mui.system.sx
import react.*
import web.html.HTMLElement

data class UstadScreenTabsState(val height: Int = 48)

typealias TabSizeStateInstance = StateInstance<UstadScreenTabsState>

val UstadScreenTabsStateContext = createContext<TabSizeStateInstance>()


/**
 * UstadScreenTabs will take a list of TabItem (each one specifying a screen name, argument map, and
 * label string) and create MUI tabs for the given list.
 */
external interface UstadScreenTabsProps: Props {

    var tabs: List<TabItem>

}

val UstadScreenTabs = FC<UstadScreenTabsProps> { props ->

    var currentTab by useState { "0" }

    val theme by useRequiredContext(ThemeContext)
    val tabSizeState = useState { UstadScreenTabsState() }
    var tabSizeStateVar by tabSizeState

    val tabsRef = useRef<HTMLElement>(null)

    useEffect(tabsRef.current?.clientHeight) {
        tabSizeStateVar = tabSizeStateVar.copy(
            height = tabsRef.current?.clientHeight ?: UstadScreenTabsState().height
        )
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
                    }

                    Tabs {
                        ref = tabsRef

                        sx {
                            position = Position.sticky
                        }

                        value = currentTab

                        onChange = { evt, newTab ->
                            currentTab = newTab
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
                    }
                }
            }
        }
    }


}