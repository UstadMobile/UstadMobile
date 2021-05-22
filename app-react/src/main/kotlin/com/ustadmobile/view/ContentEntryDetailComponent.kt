package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.model.statemanager.ToolbarTabs
import com.ustadmobile.util.CssStyleManager.tabsContainer
import com.ustadmobile.util.RouteManager.getArgs
import com.ustadmobile.util.StateManager
import react.Component
import react.RBuilder
import react.RProps
import react.setState
import styled.css
import styled.styledDiv
import kotlin.reflect.KClass

class ContentEntryDetailComponent(mProps: RProps): UstadDetailComponent<ContentEntry>(mProps), ContentEntryDetailView {

    private lateinit var mPresenter: ContentEntryDetailPresenter

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = ContentEntryDetailView.VIEW_NAME

    private var selectedTab: Any = ContentEntryDetailOverviewView.VIEW_NAME

    override var tabs: List<String>? = null
        get() = field
        set(value) {
            field = value
            val labels = value?.map { getString(
                viewNameToTitleMap[it.substringBefore("?")]?:0) }
            if(labels != null){
                StateManager.dispatch(ToolbarTabs(labels, selected = selectedTab,
                    keys = viewNameToTitleMap.keys.toList()) {
                    setState { selectedTab = it }
                })
            }
        }

    override var entity: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
        }


    override fun onComponentReady() {
        mPresenter = ContentEntryDetailPresenter(this, getArgs(), this, di, this)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledDiv {
            css { +tabsContainer }
            val vClass = viewNameToComponentMap[selectedTab]
            if(vClass != null){
                child(vClass){}
            }
        }

    }

    override fun componentWillUnmount() {
        super.componentWillUnmount()
        mPresenter.onDestroy()
    }

    companion object{
        val viewNameToComponentMap = mapOf<String, KClass<out Component<RProps, *>>>(
            ContentEntryDetailOverviewView.VIEW_NAME to ContentEntryDetailOverviewComponent::class,
            ContentEntryDetailAttemptsListView.VIEW_NAME to ContentEntryListComponent::class
        )

        val viewNameToTitleMap = mapOf(
            ContentEntryDetailOverviewView.VIEW_NAME to MessageID.overview,
            ContentEntryDetailAttemptsListView.VIEW_NAME to MessageID.attempts
        )
    }

}