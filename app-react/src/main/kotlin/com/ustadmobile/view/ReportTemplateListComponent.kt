package com.ustadmobile.view

import com.ustadmobile.core.controller.ReportTemplateListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ReportTemplateListView
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.util.UmProps
import com.ustadmobile.view.ext.renderListItemWithLeftIconTitleAndDescription
import react.RBuilder
import react.setState


class ReportTemplateListComponent(props: UmProps): UstadListComponent<Report,
        Report>(props), ReportTemplateListView {

    private var mPresenter: ReportTemplateListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in Report>?
        get() = mPresenter

    override val displayTypeRepo: Any?
        get() = dbRepo?.reportDao


    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.choose_template)
        fabManager?.text = getString(MessageID.content)
        mPresenter = ReportTemplateListPresenter(this, arguments, this, di,
            this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderListItem(item: Report) {
        val reportTitle = REPORT_TITLE_TO_ID[item.reportTitleId]?.let {
            getString(it)
        } ?: item.reportTitle

        val reportDesc = REPORT_TITLE_TO_ID[item.reportDescId]?.let {
            getString(it)
        } ?: item.reportDescription

        renderListItemWithLeftIconTitleAndDescription(
            "pie_chart",
            reportTitle ?: "", reportDesc,
            onMainList = true)

    }

    override fun handleClickEntry(entry: Report) {
        mPresenter?.handleClickEntry(entry)
    }

    override fun onFabClicked() {
        setState {
            showAddEntryOptions = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }

    companion object {
        val REPORT_TITLE_TO_ID : HashMap<Int, Int> = hashMapOf(
            Report.BLANK_REPORT to MessageID.blank_report,
            Report.BLANK_REPORT_DESC to MessageID.start_from_scratch ,
            Report.CONTENT_USAGE_OVER_TIME to MessageID.content_usage_over_time,
            Report.CONTENT_USAGE_OVER_TIME_DESC to MessageID.total_content_usage_duration_class ,
            Report.UNIQUE_CONTENT_USERS_OVER_TIME to MessageID.unique_content_users_over_time,
            Report.UNIQUE_CONTENT_USERS_OVER_TIME_DESC to MessageID.number_of_active_users_over_time ,
            Report.ATTENDANCE_OVER_TIME_BY_CLASS to MessageID.attendance_over_time_by_class,
            Report.ATTENDANCE_OVER_TIME_BY_CLASS_DESC to MessageID.percentage_of_students_attending_over_time ,
            Report.CONTENT_USAGE_BY_CLASS to MessageID.content_usage_by_class,
            Report.CONTENT_USAGE_BY_CLASS_DESC to MessageID.total_content_usage_duration_class ,
            Report.CONTENT_COMPLETION to MessageID.content_completion,
            Report.CONTENT_COMPLETION_DESC to MessageID.number_of_students_completed_time
        )
    }
}