package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.aakira.napier.Napier
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentReportEditBinding
import com.toughra.ustadmobile.databinding.ItemReportEditFilterBinding
import com.toughra.ustadmobile.databinding.ItemReportEditSeriesBinding
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToEditEntity


interface ReportEditFragmentEventHandler {
    fun onClickNewFilter(series: ReportSeries)
    fun onClickRemoveFilter(filter: ReportFilter)
    fun onClickNewSeries()
    fun onClickRemoveSeries(reportSeries: ReportSeries)
}

class ReportEditFragment : UstadEditFragment<ReportWithSeriesWithFilters>(), ReportEditView,
        ReportEditFragmentEventHandler,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mBinding: FragmentReportEditBinding? = null

    private var mPresenter: ReportEditPresenter? = null

    private var seriesRecyclerView: RecyclerView? = null

    private var seriesAdapter: RecyclerViewSeriesAdapter? = null

    override val viewContext: Any
        get() = requireContext()

    override val mEditPresenter: UstadEditPresenter<*, ReportWithSeriesWithFilters>?
        get() = mPresenter

    class SeriesViewHolder(val itemBinding: ItemReportEditSeriesBinding,
                           val activityEventHandler: ReportEditFragmentEventHandler,
                           var presenter: ReportEditPresenter?) : RecyclerView.ViewHolder(itemBinding.root){

        val filterAdapter = RecyclerViewFilterAdapter(activityEventHandler, presenter)

        var filterList: List<ReportFilter>? = null
            set(value){
                field = value
                filterAdapter.submitList(value)
            }
    }

    class RecyclerViewSeriesAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                    var presenter: ReportEditPresenter?)
        : ListAdapter<ReportSeries, SeriesViewHolder>(DIFF_CALLBACK_SERIES) {

        var visualOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>? = null
        var dataSetOptions: List<ReportEditPresenter.DataSetMessageIdOption>? = null
        var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>? = null
        var showDeleteButton: Boolean = false
        val boundSeriesViewHolder = mutableListOf<SeriesViewHolder>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
            return SeriesViewHolder(ItemReportEditSeriesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                eventHandler = activityEventHandler
            }, activityEventHandler, presenter)
        }

        override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
            val series =  getItem(position)
            holder.itemBinding.series = series
            holder.itemBinding.visualTypeOptions = visualOptions
            holder.itemBinding.dataSetOptions = dataSetOptions
            holder.itemBinding.subgroupOptions = subGroupOptions
            holder.itemBinding.showDeleteButton = showDeleteButton
            boundSeriesViewHolder += holder

            val filterRecyclerView = holder.itemBinding.itemReportEditFilterList

            holder.filterList = series.reportSeriesFilters
            filterRecyclerView.adapter = holder.filterAdapter
            filterRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        }

        override fun onViewRecycled(holder: SeriesViewHolder) {
            super.onViewRecycled(holder)
            boundSeriesViewHolder -= holder
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            boundSeriesViewHolder.clear()
        }
    }

    class FilterViewHolder(val itemBinding: ItemReportEditFilterBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewFilterAdapter(val activityEventHandler: ReportEditFragmentEventHandler?,
                                    var presenter: ReportEditPresenter?)
        : ListAdapter<ReportFilter, FilterViewHolder>(DIFF_CALLBACK_FILTER) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
            return FilterViewHolder(ItemReportEditFilterBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                eventHandler = activityEventHandler
            })
        }

        override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
            holder.itemBinding.filter = getItem(position)
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentReportEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.eventHandler = this
            it.xAxisSelectionListener = this
            seriesRecyclerView = it.activityReportEditSeriesList
        }

        seriesAdapter = RecyclerViewSeriesAdapter(this, null)
        seriesRecyclerView?.adapter = seriesAdapter
        seriesRecyclerView?.layoutManager = LinearLayoutManager(requireContext())

        mPresenter = ReportEditPresenter(requireContext(), arguments.toStringMap(), this,
                di, viewLifecycleOwner)

        seriesAdapter?.presenter = mPresenter

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.create_a_new_report, R.string.edit_report)

        val navController = findNavController()

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

        navController.currentBackStackEntry?.savedStateHandle?.observeResult(this,
                ReportFilter::class.java) {
            val filter = it.firstOrNull() ?: return@observeResult
            mPresenter?.handleAddFilter(filter)
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                onSaveStateToBackStackStateHandle()
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        seriesRecyclerView = null
        seriesAdapter = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.create_a_new_report, R.string.edit_report)
    }

    override var entity: ReportWithSeriesWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
            mBinding?.xAxisOptions = this.xAxisOptions
            seriesAdapter?.submitList(value?.reportSeriesWithFiltersList)
            val showDeleteButton = (value?.reportSeriesWithFiltersList?.size ?: 0) > 1
            seriesAdapter?.showDeleteButton = showDeleteButton
            seriesAdapter?.boundSeriesViewHolder?.forEach {
                it.itemBinding.showDeleteButton = showDeleteButton
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var titleErrorText: String? = ""
        get() = field
        set(value) {
            field = value
            mBinding?.titleErrorText = value
        }

    override var visualTypeOptions: List<ReportEditPresenter.VisualTypeMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            seriesAdapter?.visualOptions = value
        }

    override var dataSetOptions: List<ReportEditPresenter.DataSetMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            seriesAdapter?.dataSetOptions = value
        }

    override var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            mBinding?.xAxisOptions = value
        }

    override var subGroupOptions: List<ReportEditPresenter.SubGroupByMessageIdOption>? = null
        get() = field
        set(value) {
            field = value
            seriesAdapter?.subGroupOptions = value
            seriesAdapter?.boundSeriesViewHolder?.forEach {
                it.itemBinding.subgroupOptions = value
            }
        }

    companion object {

        val DIFF_CALLBACK_SERIES = object : DiffUtil.ItemCallback<ReportSeries>() {
            override fun areItemsTheSame(oldItem: ReportSeries, newItem: ReportSeries): Boolean {
                return oldItem.reportSeriesUid == newItem.reportSeriesUid
            }

            override fun areContentsTheSame(oldItem: ReportSeries,
                                            newItem: ReportSeries): Boolean {
                return oldItem === newItem
            }
        }

        val DIFF_CALLBACK_FILTER = object : DiffUtil.ItemCallback<ReportFilter>() {
            override fun areItemsTheSame(oldItem: ReportFilter, newItem: ReportFilter): Boolean {
                return oldItem.reportFilterUid == newItem.reportFilterUid
            }

            override fun areContentsTheSame(oldItem: ReportFilter,
                                            newItem: ReportFilter): Boolean {
                return oldItem === newItem
            }
        }
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        mPresenter?.handleXAxisSelected(selectedOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

    override fun onClickNewFilter(series: ReportSeries) {
        onSaveStateToBackStackStateHandle()
        navigateToEditEntity(ReportFilter().apply {
            reportFilterSeriesUid = series.reportSeriesUid
        }, R.id.report_filter_edit_dest,
                ReportFilter::class.java)
    }

    override fun onClickRemoveFilter(filter: ReportFilter) {
        mPresenter?.handleRemoveFilter(filter)
    }

    override fun onClickNewSeries() {
        mPresenter?.handleClickAddSeries()
    }

    override fun onClickRemoveSeries(reportSeries: ReportSeries) {
        mPresenter?.handleRemoveSeries(reportSeries)
    }

}