package com.ustadmobile.view

import com.ustadmobile.core.controller.ClazzAssignmentListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ClazzAssignmentListView
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.util.UmProps
import react.RBuilder
import react.setState

class AssignmentListComponent(mProps: UmProps): UstadListComponent<ClazzAssignmentWithMetrics, ClazzAssignmentWithMetrics>(mProps),
    ClazzAssignmentListView {

    private var mPresenter: ClazzAssignmentListPresenter? = null

    override val viewName: String
        get() = ClazzAssignmentListView.VIEW_NAME

    override val listPresenter: UstadListPresenter<*, in ClazzAssignmentWithMetrics>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzAssignmentDao

    override fun handleClickEntry(entry: ClazzAssignmentWithMetrics) {
        mPresenter?.handleClickEntry(entry)
    }

    override var clazzTimeZone: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.text = getString(MessageID.clazz_assignment)
        mPresenter = ClazzAssignmentListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: ClazzAssignmentWithMetrics) {
        console.log(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}