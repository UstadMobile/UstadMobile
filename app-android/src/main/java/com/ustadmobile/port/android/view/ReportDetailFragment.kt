package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportDetailBinding
import com.toughra.ustadmobile.databinding.ItemReportChartHeaderBinding
import com.toughra.ustadmobile.databinding.ItemReportStatementListBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ReportDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ReportWithFilters
import com.ustadmobile.lib.db.entities.StatementListReport
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.util.PagedListSubmitObserver
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ReportDetailFragmentEventHandler {
    fun onClickAddToDashboard(report: ReportWithFilters)
}

class ReportDetailFragment : UstadDetailFragment<ReportWithFilters>(), ReportDetailView, ReportDetailFragmentEventHandler {

    private var mBinding: FragmentReportDetailBinding? = null

    private var mPresenter: ReportDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var chartAdapter: RecyclerViewChartAdapter? = null

    private var statementAdapter: StatementViewRecyclerAdapter? = null

    private var mergeAdapter: MergeAdapter? = null

    private var reportRecyclerView: RecyclerView? = null

    var dbRepo: UmAppDatabase? = null

    class ChartViewHolder(val itemBinding: ItemReportChartHeaderBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewChartAdapter(val activityEventHandler: ReportDetailFragmentEventHandler,
                                   var presenter: ReportDetailPresenter?) : ListAdapter<ReportGraphHelper.ChartData, ChartViewHolder>(DIFFUTIL_CHART) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
            return ChartViewHolder(ItemReportChartHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                eventHandler = activityEventHandler
            })
        }

        override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.chart = item
            holder.itemBinding.previewChartView.setChartData(item)
        }

    }

    class StatementViewRecyclerAdapter(
            val activityEventHandler: ReportDetailFragmentEventHandler,
            var presenter: ReportDetailPresenter?) :
            PagedListAdapter<StatementListReport,
                    StatementViewRecyclerAdapter.StatementViewHolder>(DIFFUTIL_STATEMENT) {

        class StatementViewHolder(val binding: ItemReportStatementListBinding) :
                RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatementViewHolder {
            return StatementViewHolder(ItemReportStatementListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
            })
        }

        override fun onBindViewHolder(holder: StatementViewHolder, position: Int) {
            holder.binding.report = getItem(position)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }


    private var statementListObserver: Observer<PagedList<StatementListReport>>? = null


    private var currentLiveData: LiveData<PagedList<StatementListReport>>? = null

    override var statementList: DataSource.Factory<Int, StatementListReport>? = null
        get() = field
        set(value) {
            val statementObsVal = statementListObserver ?: return
            currentLiveData?.removeObserver(statementObsVal)
            val displayTypeRepoVal = dbRepo?.statementDao ?: return
            currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
            currentLiveData?.observe(this, statementObsVal)
            field = value
        }


    override var chartData: ReportGraphHelper.ChartData? = null
        get() = field
        set(value) {
            field = value
            chartAdapter?.submitList(listOf(value))
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentReportDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        val accountManager: UstadAccountManager by instance()
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)
        reportRecyclerView = rootView.findViewById(R.id.fragment_detail_report_list)
        chartAdapter = RecyclerViewChartAdapter(this, null)
        statementAdapter = StatementViewRecyclerAdapter(this, null).also {
            statementListObserver = PagedListSubmitObserver(it)
        }

        mergeAdapter = MergeAdapter(chartAdapter, statementAdapter)
        reportRecyclerView?.adapter = mergeAdapter
        reportRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ReportDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        chartAdapter?.presenter = mPresenter
        statementAdapter?.presenter = mPresenter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.onClickListener = {
            val report = entity
            if (report == null || report.reportUid == 0L) {
                findNavController().popBackStack()
            } else mPresenter?.handleClickEdit()

        }

        val navController = findNavController()
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())


    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        chartAdapter = null
        statementAdapter = null
        mergeAdapter = null
        dbRepo = null
        chartData = null
        currentLiveData = null
    }

    override var entity: ReportWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
            clazzWorkTitle = value?.reportTitle
        }


    override fun onClickAddToDashboard(report: ReportWithFilters) {
        mPresenter?.handleOnClickAddFromDashboard(report)
        if (report.reportUid == 0L) {
            findNavController().popBackStack(R.id.report_edit_dest, true)
        }
    }


    companion object {

        val DIFFUTIL_STATEMENT = object : DiffUtil.ItemCallback<StatementListReport>() {
            override fun areItemsTheSame(oldItem: StatementListReport, newItem: StatementListReport): Boolean {
                return oldItem.statementUid == newItem.statementUid
            }

            override fun areContentsTheSame(oldItem: StatementListReport, newItem: StatementListReport): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_CHART = object : DiffUtil.ItemCallback<ReportGraphHelper.ChartData>() {
            override fun areItemsTheSame(oldItem: ReportGraphHelper.ChartData, newItem: ReportGraphHelper.ChartData): Boolean {
                return oldItem.reportWithFilters.reportUid == newItem.reportWithFilters.reportUid
            }

            override fun areContentsTheSame(oldItem: ReportGraphHelper.ChartData, newItem: ReportGraphHelper.ChartData): Boolean {
                return oldItem == newItem
            }
        }

    }

}