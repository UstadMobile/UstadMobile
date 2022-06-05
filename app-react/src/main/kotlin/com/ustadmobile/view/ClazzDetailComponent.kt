package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.urlSearchParamsToMap
import react.RBuilder
import react.setState

class ClazzDetailComponent(mProps: UmProps): UstadDetailComponent<Clazz>(mProps), ClazzDetailView {

    private var mPresenter: ClazzDetailPresenter? = null

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

    override var entity: Clazz? = null
        get() = field
        set(value) {
            ustadComponentTitle = entity?.clazzName
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = ClazzDetailPresenter(this, arguments, this,di,this)
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
            ClazzDetailOverviewView.VIEW_NAME to MessageID.overview,
            ContentEntryList2View.VIEW_NAME to MessageID.content,
            ClazzMemberListView.VIEW_NAME to MessageID.members,
            ClazzLogListAttendanceView.VIEW_NAME to MessageID.attendance,
            CourseGroupSetListView.VIEW_NAME to MessageID.groups
        )

    }
}