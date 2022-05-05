package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.ClazzAssignmentDetailStudentProgressOverviewListView
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.urlSearchParamsToMap
import react.RBuilder
import react.setState

class ClazzAssignmentDetailComponent(mProps: UmProps): UstadDetailComponent<ClazzAssignment>(mProps),
    ClazzAssignmentDetailView {

    private var mPresenter: ClazzAssignmentDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var tabsToRender: List<UmTab>? = null

    override var tabs: List<String>? = null
        set(value) {
            field = value
            tabsToRender = value?.mapIndexed{ index, it ->
                val messageId = VIEWNAME_TO_TITLE_MAP[it.substringBefore("?",)] ?: 0
                UmTab(index,
                    it.substringBefore("?"),
                    urlSearchParamsToMap(it.substring(it.lastIndexOf("?"))),
                    getString(messageId)
                )
            }?.toList()

        }

    override var entity: ClazzAssignment? = null
        get() = field
        set(value) {
            ustadComponentTitle = entity?.caTitle
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ClazzAssignmentDetailPresenter(this, arguments, this,di,this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {

        tabsToRender?.let { tabs ->
            renderTabs(tabs, activeTabIndex = arguments[UstadView.ARG_ACTIVE_TAB_INDEX]?.toInt() ?: 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        entity = null
        tabsToRender = null
        tabs = null
    }

    companion object {
        val VIEWNAME_TO_TITLE_MAP = mapOf(
            ClazzAssignmentDetailOverviewView.VIEW_NAME to MessageID.overview,
            ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME to MessageID.submissions,
        )

    }
}