package com.ustadmobile.view

import com.ustadmobile.core.view.UstadView.Companion.ARG_ACTIVE_TAB_INDEX
import com.ustadmobile.door.ext.toUrlQueryString
import com.ustadmobile.mui.components.*
import com.ustadmobile.navigation.RouteManager.lookupDestinationName
import com.ustadmobile.util.StyleManager.displayProperty
import com.ustadmobile.util.StyleManager.tabsContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import com.ustadmobile.util.getViewNameFromUrl
import com.ustadmobile.util.urlSearchParamsToMap
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

class  TabsComponent(mProps: TabsProps): UstadBaseComponent<TabsProps,UmState>(mProps){

    private lateinit var selectedTabTitle: String

    private val tabChangeListener:(Any)-> Unit = {
        setState {
            updateUrl(it.toString())
            selectedTabTitle = it.toString()
        }
    }

    private fun updateUrl(selected: String){
        val index = props.tabs.indexOfFirst { it.title == selected }
        val params = urlSearchParamsToMap().toMutableMap()
        if(params[ARG_ACTIVE_TAB_INDEX] ?:0 == index) return
        params[ARG_ACTIVE_TAB_INDEX] = index.toString()
        window.location.replace("#/${getViewNameFromUrl()}?${params.toUrlQueryString()}")
    }

    override fun UmState.init(props: TabsProps) {
        selectedTabTitle = props.tabs[props.activeTabIndex].title
    }

    override fun onCreateView() {
        super.onCreateView()
        updateUrl(selectedTabTitle)
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