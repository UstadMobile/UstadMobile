package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemReportListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ReportListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.generateChartData
import com.ustadmobile.core.util.safeParseList
import com.ustadmobile.core.view.ReportListView
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.core.R as CR

class ReportListFragment() : UstadListViewFragment<Report, Report>(),
        ReportListView, View.OnClickListener {

    private var mPresenter: ReportListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in Report>?
        get() = mPresenter

    class ReportListViewHolder(val itemBinding: ItemReportListBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class ReportListRecyclerAdapter(var presenter: ReportListPresenter?, val dbRepo: UmAppDatabase?,
                                    val di: DI)
        : SelectablePagedListAdapter<Report, ReportListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportListViewHolder {
            val itemBinding = ItemReportListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ReportListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ReportListViewHolder, position: Int) {
            val item = getItem(position) ?: Report()
            holder.itemBinding.report = item
            holder.itemView.tag = holder.itemBinding.report?.reportUid
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
            (holder.itemBinding.listReportChart.getTag(R.id.tag_graphlookup_key) as? Job)?.cancel()
            val graphJob = GlobalScope.async(Dispatchers.Main) {
                try {
                    val series = if (!item.reportSeries.isNullOrEmpty()) {
                        safeParseList(di, ListSerializer(ReportSeries.serializer()),
                                ReportSeries::class, item.reportSeries ?: "")
                    } else {
                        listOf()
                    }
                    val accountManager: UstadAccountManager = di.direct.instance()
                    val reportWithSeriesWithFilters = ReportWithSeriesWithFilters(item, series)

                    val chartData = dbRepo?.generateChartData(reportWithSeriesWithFilters,
                            holder.itemView.context, di.direct.instance(), accountManager.activeAccount.personUid)
                    holder.itemBinding.listReportChart.setChartData(chartData)
                } catch (e: Exception) {
                    return@async
                }
            }
            holder.itemBinding.listReportChart.setTag(R.id.tag_graphlookup_key, graphJob)

        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ReportListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(CR.string.create_a_new_report),
                onClickSort = this, sortOrderOption = mPresenter?.sortOptions?.get(0))
        mDataRecyclerViewAdapter = ReportListRecyclerAdapter(mPresenter, dbRepo, di)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(CR.string.report)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(v: View?) {
        if (v?.id == R.id.item_createnew_layout)
            mPresenter?.handleClickAddNewItem()
        else {
            super.onClick(v)
        }
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