package com.ustadmobile.view

import com.ustadmobile.mui.components.*
import com.ustadmobile.navigation.RouteManager.lookupDestinationName
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.tabsContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinext.js.jsObject
import kotlinx.browser.window
import kotlinx.css.*
import react.RBuilder
import react.setState
import styled.css
import styled.styledDiv

interface TabsProps: UmProps {
    var tabs: List<UmTab>
    var showTabs: Boolean
    var activeTabIndex: Int
}

data class UmTab(var index: Int, var viewName: String, val args: Map<String,String>, var title: String)

class TabsComponent(mProps: TabsProps): UstadBaseComponent<TabsProps,UmState>(mProps){

    private lateinit var selectedTabTitle: String

    private val tabChangeListener:(Any)-> Unit = {
        setState {
            updateTabIndexState(it.toString())
            selectedTabTitle = it.toString()
        }
    }

    private fun updateTabIndexState(selected: String) {
        val index = props.tabs.indexOfFirst { it.title == selected }
        val state = window.history.state ?: jsObject()
        state.asDynamic().tabIndex = index
        window.history.replaceState(state, "")
    }

    override fun UmState.init(props: TabsProps) {
        selectedTabTitle = props.tabs[props.activeTabIndex].title
    }

    override fun onCreateView() {
        super.onCreateView()

        val tabIndex = window.history.state?.asDynamic()?.tabIndex
        if(tabIndex != js("undefined") && tabIndex != 0) {
            //Maybe there is a better way to do this so we don't re-render?
            setState {
                val tabIndexInt: Int = tabIndex.unsafeCast<Int>()
                selectedTabTitle = props.tabs[tabIndexInt].title
            }
        }
    }

    override fun RBuilder.render() {
        umAppBar(position = AppBarPosition.static) {
            css{
                display = displayProperty(props.showTabs)
            }
            umTabs(selectedTabTitle,
                scrollButtons = TabScrollButtons.auto,
                variant = TabVariant.scrollable,
                onChange = { _, value ->
                    tabChangeListener(value)
                }) {
                css {
                    padding = "0 20px 0 20px"
                }
                attrs.asDynamic().id = "um-tabs"
                props.tabs.forEachIndexed { _, it ->
                    umTab(it.title, it.title) {
                        css {
                            display = Display.block
                            width = LinearDimension("100%")
                        }
                    }
                }
            }
        }
        val selectedTab = props.tabs.first { it.title == selectedTabTitle}
        val component = lookupDestinationName(selectedTab.viewName)?.component
        styledDiv {
            css(tabsContainer)
            if(component != null){
                child(component){
                    attrs.asDynamic().arguments = selectedTab.args
                }
            }
        }
    }
}

fun RBuilder.renderTabs(
    tabs: List<UmTab>,
    showTabs: Boolean = true,
    activeTabIndex: Int
) = child(TabsComponent::class) {
    attrs.tabs = tabs
    attrs.showTabs = showTabs
    attrs.activeTabIndex = activeTabIndex
}