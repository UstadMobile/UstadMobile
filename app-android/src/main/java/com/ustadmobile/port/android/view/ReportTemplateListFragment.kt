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
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter


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
        ustadFragmentTitle = getString(R.string.choose_template, context)

        mPresenter = ReportTemplateListPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter()
        mDataRecyclerViewAdapter = ReportTemplateRecyclerAdapter(mPresenter)
        return view
    }

    override fun onResume() {
        super.onResume()
        ustadFragmentTitle = getString(R.string.choose_template, context)
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