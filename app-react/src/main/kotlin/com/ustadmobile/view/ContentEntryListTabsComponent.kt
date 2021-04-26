package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.ContentEntryList2View.Companion.ARG_CONTENT_FILTER
import com.ustadmobile.core.view.ContentEntryListTabsView
import com.ustadmobile.model.statemanager.ToolbarTabs
import com.ustadmobile.util.CssStyleManager.tabsContainer
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import kotlinx.coroutines.selects.select
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledDiv

class ContentEntryListTabsComponent(mProps: RProps) :UstadBaseComponent<RProps, RState>(mProps),
    ContentEntryListTabsView {

    private var selectedTab: Any = ""

    private val tabChangeListener:(Any)-> Unit = {
        val args = getArgs()
        args[ARG_CONTENT_FILTER] = viewNameFilterMap[it]?:""
        systemImpl.go(ContentEntryListTabsView.VIEW_NAME,args,this)
    }

    override fun onComponentRefreshed(viewName: String?) {
        super.onComponentRefreshed(viewName)
        if(viewName == ContentEntryListTabsView.VIEW_NAME){
            selectedTab = getSelectedFilter()
        }
    }


    override fun componentDidMount() {
        super.componentDidMount()
        selectedTab = getSelectedFilter()
        val tabs = listOf(systemImpl.getString(MessageID.libraries, this),
        systemImpl.getString(MessageID.downloaded, this))
        StateManager.dispatch(ToolbarTabs(tabs, listOf(MessageID.libraries,MessageID.downloaded),
            getSelectedFilter(), tabChangeListener))
    }

    private fun getSelectedFilter(): Int {
        return  viewNameFilterMap.toList().firstOrNull {
            it.second == getArgs()[ARG_CONTENT_FILTER]}?.first?:MessageID.libraries
    }

    override fun RBuilder.render() {
        styledDiv {
            css { +tabsContainer }
            if(selectedTab.toString().isNotEmpty()){
                child(ContentEntryListComponent::class){}
            }
        }
    }

    companion object {
        val viewNameFilterMap = mapOf(
            MessageID.libraries to ContentEntryList2View.ARG_LIBRARIES_CONTENT,
            MessageID.downloaded to ContentEntryList2View.ARG_DOWNLOADED_CONTENT
        )
    }
}