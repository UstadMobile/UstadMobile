package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemReportTemplateListBinding
import com.ustadmobile.core.controller.ReportTemplateListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportTemplateListView
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.Report.Companion.BLANK_REPORT
import com.ustadmobile.lib.db.entities.Report.Companion.BLANK_REPORT_DESC
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_USAGE_OVER_TIME
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_USAGE_OVER_TIME_DESC
import com.ustadmobile.lib.db.entities.Report.Companion.UNIQUE_CONTENT_USERS_OVER_TIME
import com.ustadmobile.lib.db.entities.Report.Companion.UNIQUE_CONTENT_USERS_OVER_TIME_DESC
import com.ustadmobile.lib.db.entities.Report.Companion.ATTENDANCE_OVER_TIME_BY_CLASS
import com.ustadmobile.lib.db.entities.Report.Companion.ATTENDANCE_OVER_TIME_BY_CLASS_DESC
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_USAGE_BY_CLASS
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_USAGE_BY_CLASS_DESC
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_COMPLETION
import com.ustadmobile.lib.db.entities.Report.Companion.CONTENT_COMPLETION_DESC
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import com.ustadmobile.core.R as CR


class ReportTemplateListFragment(): UstadListViewFragment<Report, Report>(),
        ReportTemplateListView{

    private var mPresenter: ReportTemplateListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in Report>?
        get() = mPresenter

    class ReportTemplateRecyclerAdapter(var presenter: ReportTemplateListPresenter?):
            SelectablePagedListAdapter<Report,
                    ReportTemplateRecyclerAdapter.ReportTemplateListViewHolder>(DIFF_CALLBACK) {

        class ReportTemplateListViewHolder(val itemBinding: ItemReportTemplateListBinding): RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportTemplateListViewHolder {
            val itemBinding = ItemReportTemplateListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ReportTemplateListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ReportTemplateListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.reportTemplate = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        ustadFragmentTitle = getString(CR.string.choose_template)

        mPresenter = ReportTemplateListPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        mDataRecyclerViewAdapter = ReportTemplateRecyclerAdapter(mPresenter)
        return view
    }

    override fun onResume() {
        super.onResume()
        ustadFragmentTitle = getString(CR.string.choose_template)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.reportDao


    companion object {


        val REPORT_TITLE_TO_ID : HashMap<Int, StringResource> = hashMapOf(
            BLANK_REPORT to MR.strings.blank_report,
            BLANK_REPORT_DESC to MR.strings.start_from_scratch ,
            CONTENT_USAGE_OVER_TIME to MR.strings.content_usage_over_time,
            CONTENT_USAGE_OVER_TIME_DESC to MR.strings.total_content_usage_duration_class ,
            UNIQUE_CONTENT_USERS_OVER_TIME to MR.strings.unique_content_users_over_time,
            UNIQUE_CONTENT_USERS_OVER_TIME_DESC to MR.strings.number_of_active_users_over_time ,
            ATTENDANCE_OVER_TIME_BY_CLASS to MR.strings.attendance_over_time_by_class,
            ATTENDANCE_OVER_TIME_BY_CLASS_DESC to MR.strings.percentage_of_students_attending_over_time ,
            CONTENT_USAGE_BY_CLASS to MR.strings.content_usage_by_class,
            CONTENT_USAGE_BY_CLASS_DESC to MR.strings.total_content_usage_duration_class ,
            CONTENT_COMPLETION to MR.strings.content_completion,
            CONTENT_COMPLETION_DESC to MR.strings.number_of_students_completed_time
        )


        val DIFF_CALLBACK: DiffUtil.ItemCallback<Report> = object
            : DiffUtil.ItemCallback<Report>() {
            override fun areItemsTheSame(oldItem: Report,
                                         newItem: Report): Boolean {
                return oldItem.reportUid == newItem.reportUid
            }

            override fun areContentsTheSame(oldItem: Report,
                                            newItem: Report): Boolean {
                return oldItem == newItem
            }
        }
    }

}