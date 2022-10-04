package com.ustadmobile.port.android.view

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
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
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.ext.ChartData
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.StatementEntityWithDisplayDetails
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs


interface ReportDetailFragmentEventHandler {
    fun onClickAddToDashboard(report: ReportWithSeriesWithFilters)
    fun onClickExportButton()
    fun onClickAddAsTemplate(report: ReportWithSeriesWithFilters)
}

class ReportDetailFragment : UstadDetailFragment<ReportWithSeriesWithFilters>(), ReportDetailView, ReportDetailFragmentEventHandler {

    private var mBinding: FragmentReportDetailBinding? = null

    private var mPresenter: ReportDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var chartAdapter: RecyclerViewChartAdapter? = null

    private var mergeAdapter: ConcatAdapter? = null

    private var reportRecyclerView: RecyclerView? = null

    var dbRepo: UmAppDatabase? = null

    class ChartViewHolder(val itemBinding: ItemReportChartHeaderBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewChartAdapter(val activityEventHandler: ReportDetailFragmentEventHandler,
                                   var presenter: ReportDetailPresenter?) : ListAdapter<ChartData, ChartViewHolder>(DIFFUTIL_CHART) {

        var saveAsTemplateVisible = false
        /**
         * saved to update the ui when changes to ui are made
         */
        var chartBinding: ItemReportChartHeaderBinding? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewHolder {
            val mBinding = ItemReportChartHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                eventHandler = activityEventHandler
            }
            return ChartViewHolder(mBinding)
        }

        override fun onBindViewHolder(holder: ChartViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.chart = item
            holder.itemBinding.saveAsTemplateVisible = saveAsTemplateVisible
            holder.itemBinding.chartView.setChartData(item)
            chartBinding = holder.itemBinding
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            chartBinding = null
            presenter = null
            chartBinding = null
        }

    }

    class StatementViewRecyclerAdapter(
            val activityEventHandler: ReportDetailFragmentEventHandler,
            var presenter: ReportDetailPresenter?) :
            PagedListAdapter<StatementEntityWithDisplayDetails,
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

    /**
     * a holder that contains a list of data sources where each has its own header and listData
     */
    class AdapterSourceHolder(val statementAdapter: StatementViewRecyclerAdapter,
                              val seriesHeaderAdapter: SimpleHeadingRecyclerAdapter,
                              val dbRepo: UmAppDatabase?,
                              val lifecycleOwner: LifecycleOwner): Observer<PagedList<StatementEntityWithDisplayDetails>> {

        val adapter = ConcatAdapter(seriesHeaderAdapter, statementAdapter)

        private var currentLiveData: LiveData<PagedList<StatementEntityWithDisplayDetails>>? = null

        var source: DataSource.Factory<Int, StatementEntityWithDisplayDetails>? = null
            set(value) {
                currentLiveData?.removeObserver(this)
                val displayTypeRepoVal = dbRepo?.statementDao ?: return
                currentLiveData = value?.asRepositoryLiveData(displayTypeRepoVal)
                currentLiveData?.observe(lifecycleOwner, this)
                field = value
            }

        override fun onChanged(t: PagedList<StatementEntityWithDisplayDetails>?) {
            statementAdapter.submitList(t)
        }

    }

    private var adapterSourceHolderList = mutableListOf<AdapterSourceHolder>()

    override var saveAsTemplateVisible: Boolean = false
        get() = field
        set(value) {
            field = value
            chartAdapter?.saveAsTemplateVisible = value
            chartAdapter?.chartBinding?.saveAsTemplateVisible = value
        }

    override var statementListDetails: List<DataSource.Factory<Int, StatementEntityWithDisplayDetails>>? = null
        get() = field
        set(value) {

            val sizeDiff = (value?.size ?: 0) - adapterSourceHolderList.size
            if (sizeDiff > 0) {

                repeat(sizeDiff) {
                    val statementAdapter = StatementViewRecyclerAdapter(this, mPresenter)
                    val seriesHeaderAdapter = SimpleHeadingRecyclerAdapter(chartData?.seriesData?.get(it)?.series?.reportSeriesName
                            ?: "Label not found")
                    seriesHeaderAdapter.visible = true
                    val sourceHolder = AdapterSourceHolder(statementAdapter, seriesHeaderAdapter, dbRepo, this)
                    adapterSourceHolderList.add(sourceHolder)
                    mergeAdapter?.addAdapter(sourceHolder.adapter)
                }

            } else if (sizeDiff < 0) {

                repeat(abs(sizeDiff)){
                    val holder = adapterSourceHolderList.removeAt(it)
                    holder.source = null
                    mergeAdapter?.removeAdapter(holder.adapter)
                }
            }

            adapterSourceHolderList.forEachIndexed { idx, holder ->
                holder.seriesHeaderAdapter.headingText = chartData?.seriesData?.get(idx)?.series?.reportSeriesName ?: "Label not found"
                holder.source = value?.get(idx)
            }

            field = value
        }


    override var chartData: ChartData? = null
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
        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)
        reportRecyclerView = rootView.findViewById(R.id.fragment_detail_report_list)
        chartAdapter = RecyclerViewChartAdapter(this, null)

        mergeAdapter = ConcatAdapter(chartAdapter)
        reportRecyclerView?.adapter = mergeAdapter
        reportRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ReportDetailPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner).withViewLifecycle()

        chartAdapter?.presenter = mPresenter

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
        mergeAdapter = null
        dbRepo = null
        chartData = null
        adapterSourceHolderList.forEach {
            it.source = null
        }
        adapterSourceHolderList.clear()
        reportRecyclerView = null
    }

    override var entity: ReportWithSeriesWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
            ustadFragmentTitle = value?.reportTitle
        }


    override fun onClickAddToDashboard(report: ReportWithSeriesWithFilters) {
        mPresenter?.handleOnClickAddFromDashboard(report)
        if (report.reportUid == 0L) {
            findNavController().popBackStack(R.id.report_edit_dest, true)
            findNavController().popBackStack(R.id.report_template_list_dest, true)
        }
    }

    override fun onClickExportButton() {
        GlobalScope.launch(Dispatchers.IO) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, saveBitmapAndGetUriFromFile(chartAdapter?.chartBinding?.
                chartView?.chartView?.chartBitmap))
                type = "image/png"
            }
            startActivity(Intent.createChooser(intent, requireContext().getString(R.string.export)))
        }
    }

    fun saveBitmapAndGetUriFromFile(bmp: Bitmap?): Uri? {
        var bmpUri: Uri? = null
        try {

            val file = File(context?.externalCacheDir,
                    System.currentTimeMillis().toString() + ".png")
            val out = FileOutputStream(file)
            bmp?.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.close()
            bmpUri = FileProvider.getUriForFile(requireContext(),
                    "${context?.packageName}.provider", file)

        } catch (e: IOException) {
            showSnackBar(requireContext().getString(R.string.error))
            e.printStackTrace()
        }
        return bmpUri
    }

    override fun onClickAddAsTemplate(report: ReportWithSeriesWithFilters) {
        mPresenter?.handleOnClickAddAsTemplate(report)
        showSnackBar(requireContext().getString(R.string.added))
    }


    companion object {

        val DIFFUTIL_STATEMENT = object : DiffUtil.ItemCallback<StatementEntityWithDisplayDetails>() {
            override fun areItemsTheSame(oldItem: StatementEntityWithDisplayDetails, newItem: StatementEntityWithDisplayDetails): Boolean {
                return oldItem.statementUid == newItem.statementUid
            }

            override fun areContentsTheSame(oldItem: StatementEntityWithDisplayDetails, newItem: StatementEntityWithDisplayDetails): Boolean {
                return oldItem == newItem
            }
        }

        val DIFFUTIL_CHART = object : DiffUtil.ItemCallback<ChartData>() {
            override fun areItemsTheSame(oldItem: ChartData, newItem: ChartData): Boolean {
                return oldItem.reportWithFilters.reportUid == newItem.reportWithFilters.reportUid
            }

            override fun areContentsTheSame(oldItem: ChartData, newItem: ChartData): Boolean {
                return oldItem == newItem
            }
        }

    }

}