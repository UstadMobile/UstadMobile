package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.*
import com.ustadmobile.core.controller.ReportDetailPresenter
import com.ustadmobile.core.controller.ReportEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ReportGraphHelper
import com.ustadmobile.core.util.ext.observeResult
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.core.view.ReportEditView
import com.ustadmobile.core.view.VerbEntityListView.Companion.ARG_EXCLUDE_VERBUIDS_LIST
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.port.android.view.ext.navigateToPickEntityFromList


interface ReportEditFragmentEventHandler {
    fun onClickNewFilter()
    fun onClickRemoveFilter(filter: ReportFilterWithDisplayDetails)
    fun onClickNewSeries()
    fun onClickRemoveSeries(reportSeries: ReportSeries)
}

class ReportEditFragment : UstadEditFragment<ReportWithFilters>(), ReportEditView, ReportEditFragmentEventHandler,  DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {
    private var mBinding: FragmentReportEditBinding? = null

    private var mPresenter: ReportEditPresenter? = null

    private var seriesRecyclerView: RecyclerView? = null

    private var seriesAdapter: RecyclerViewSeriesAdapter? = null

    private var filterAdapter: RecyclerViewFilterAdapter? = null

    override val viewContext: Any
        get() = requireContext()

    override val mEditPresenter: UstadEditPresenter<*, ReportWithFilters>?
        get() = mPresenter


    class SeriesViewHolder(val itemBinding: ItemReportEditSeriesBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewSeriesAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                   var presenter: ReportEditPresenter?) : ListAdapter<ReportSeries, SeriesViewHolder>(DIFF_CALLBACK_SERIES) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
            return SeriesViewHolder(ItemReportEditSeriesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                eventHandler = activityEventHandler
            })
        }

        override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
            val item = getItem(position)
        }

    }

    class FilterViewHolder(val itemBinding: ItemContentReportEditBinding) : RecyclerView.ViewHolder(itemBinding.root)

    class RecyclerViewFilterAdapter(val activityEventHandler: ReportEditFragmentEventHandler,
                                    var presenter: ReportEditPresenter?) : ListAdapter<ReportSeries, FilterViewHolder>(DIFF_CALLBACK_SERIES) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
            return FilterViewHolder(ItemContentReportEditBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                mPresenter = presenter
                this.activityEventHandler = activityEventHandler
            })
        }

        override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
            val item = getItem(position)
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

        mPresenter?.onCreate(findNavController().currentBackStackEntrySavedStateMap())

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
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.create_a_new_report, R.string.edit_report)
    }

    override var entity: ReportWithFilters? = null
        get() = field
        set(value) {
            field = value
            mBinding?.report = value
            mBinding?.xAxisOptions = this.xAxisOptions
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

    override var chartOptions: List<ReportEditPresenter.ChartTypeMessageIdOption>? = null

    override var xAxisOptions: List<ReportEditPresenter.XAxisMessageIdOption>? = null

    override var groupOptions: List<ReportEditPresenter.GroupByMessageIdOption>? = null
        get() = field
        set(value){
            field = value
        }

    companion object {

        val DIFF_CALLBACK_SERIES = object : DiffUtil.ItemCallback<ReportSeries>() {
            override fun areItemsTheSame(oldItem: ReportSeries, newItem: ReportSeries): Boolean {
                return oldItem.reportSeriesUid == newItem.reportSeriesUid
            }

            override fun areContentsTheSame(oldItem: ReportSeries,
                                            newItem: ReportSeries): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        mPresenter?.handleXAxisSelected(selectedOption)
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {

    }

    override fun onClickNewFilter() {
        TODO("Not yet implemented")
    }

    override fun onClickRemoveFilter(filter: ReportFilterWithDisplayDetails) {
        TODO("Not yet implemented")
    }

    override fun onClickNewSeries() {
        TODO("Not yet implemented")
    }

    override fun onClickRemoveSeries(reportSeries: ReportSeries) {
        TODO("Not yet implemented")
    }

}