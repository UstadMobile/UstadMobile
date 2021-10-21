package com.ustadmobile.view

import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ContentEntryDetailAttemptsListView
import com.ustadmobile.core.view.ContentEntryDetailOverviewView
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.urlSearchParamsToMap
import react.RBuilder
import react.RProps

class ContentEntryDetailComponent(mProps: RProps): UstadDetailComponent<ContentEntry>(mProps), ContentEntryDetailView {

    private var mPresenter: ContentEntryDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override val viewName: String
        get() = ContentEntryDetailView.VIEW_NAME

    private var tabsToRender: List<UstadTab>? = null

    override var tabs: List<String>? = null
        get() = field
        set(value) {
            field = value
            tabsToRender = value?.map {
                val messageId = viewNameToTitleMap[it.substringBefore("?",)] ?: 0
                UstadTab(
                    it.substringBefore("?"),
                    urlSearchParamsToMap(it.substring(it.lastIndexOf("?"))),
                    getString( messageId)
                )
            }
        }

    override var entity: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ContentEntryDetailPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        tabsToRender?.let {
            renderTabs(it, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object{
        val viewNameToTitleMap = mapOf(
            ContentEntryDetailOverviewView.VIEW_NAME to MessageID.overview,
            ContentEntryDetailAttemptsListView.VIEW_NAME to MessageID.attempts
        )
    }
}