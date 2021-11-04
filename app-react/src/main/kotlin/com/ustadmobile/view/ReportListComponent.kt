package com.ustadmobile.view

import com.ustadmobile.core.controller.ReportListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.lib.db.entities.Report
import react.RBuilder
import react.RProps

class ReportListComponent(mProps: RProps):  UstadListComponent<Report, Report>(mProps),
    ReportListView {

    private var mPresenter: ReportListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.reportDao

    override val listPresenter: UstadListPresenter<*, in Report>?
        get() = mPresenter

    override val viewName: String
        get() = ReportListView.VIEW_NAME

    override fun onCreateView() {
        super.onCreateView()
        createNewText = getString(MessageID.create_a_new_report)
        fabManager?.text = getString(MessageID.report)
        mPresenter = ReportListPresenter(this, arguments, this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: Report) {
        +"Report screen"
    }

    override fun handleClickEntry(entry: Report) {
        TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}