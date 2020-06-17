package com.ustadmobile.port.android.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.PercentFormatter
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogListAttendanceChartheaderBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogAttendanceListBinding
import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.door.DoorMutableLiveData
import com.ustadmobile.lib.db.entities.ClazzLog
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import java.text.DecimalFormat
import java.util.*

class ClazzLogListAttendanceFragment(): UstadListViewFragment<ClazzLog, ClazzLog>(),
        ClazzLogListAttendanceView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener{

    private var mPresenter: ClazzLogListAttendancePresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzLog>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var clazzTimeZone: String?
        get() = (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone
        set(value) {
            (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone = value
        }

    override var graphData: DoorMutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>? = null
        set(value) {
            val observer = graphRecyclerViewAdapter ?: return
            field?.removeObserver(observer)
            field = value
            field?.observe(viewLifecycleOwner, observer)
        }


    override var recordAttendanceButtonVisible: Boolean
        get() = fabManager?.visible ?: false
        set(value) {
            fabManager?.visible = value
        }

    private var graphRecyclerViewAdapter: ClazzLogListGraphRecyclerAdapter? = null

    class ClazzLogListViewHolder(val itemBinding: ItemClazzLogAttendanceListBinding): RecyclerView.ViewHolder(itemBinding.root)

    class ClazzLogListRecyclerAdapter(var presenter: ClazzLogListAttendancePresenter?, var clazzTimeZone: String?)
        : SelectablePagedListAdapter<ClazzLog, ClazzLogListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogListViewHolder {
            val itemBinding = ItemClazzLogAttendanceListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            itemBinding.presenter = presenter
            itemBinding.selectablePagedListAdapter = this
            return ClazzLogListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ClazzLogListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.clazzLog = item
            val timezoneVal = clazzTimeZone
            if(timezoneVal != null){
                holder.itemBinding.clazzLogLocalTime = DateTime.fromUnix(item?.logDate ?: 0L)
                        .toOffsetByTimezone(timezoneVal)
                holder.itemBinding.clazzLocalTimeZone = TimeZone.getTimeZone(timezoneVal)
            }

            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
            clazzTimeZone = null
        }
    }

    class ClazzLogListGraphRecyclerAdapter(var presenter: ClazzLogListAttendancePresenter?,
                                           var clazzTimeZone: String?,
                                           var context: Context?)
        : SingleItemRecyclerViewAdapter<ClazzLogListGraphRecyclerAdapter.GraphViewHolder>(true), Observer<ClazzLogListAttendancePresenter.AttendanceGraphData>{

        class GraphViewHolder(val binding: FragmentClazzLogListAttendanceChartheaderBinding): RecyclerView.ViewHolder(binding.root)

        private var data: LineData? = null

        private var graphDateRange: Pair<Float, Float>? = null

        override fun onChanged(t: ClazzLogListAttendancePresenter.AttendanceGraphData?) {
            val graphData = t ?: return
            val contextVal = context ?: return
            if(graphData.percentageAttendedSeries.size < 2) {
                return
            }

            val lineData = LineData().apply {
                listOf(graphData.percentageAttendedSeries, graphData.percentageLateSeries).forEachIndexed { index, list ->
                    val colorId = if(index == 0) R.color.successColor else R.color.secondaryColor
                    val seriesColor = context?.let { ContextCompat.getColor(it, colorId) } ?: Color.BLACK
                    addDataSet(LineDataSet(list.map { Entry(it.first.toFloat(), it.second * 100) },
                            contextVal.getString(R.string.attendance)).apply {
                        color = seriesColor
                        valueTextColor = Color.BLACK
                        lineWidth = 1f
                        setDrawValues(false)
                        setDrawCircles(false)
                        mode = LineDataSet.Mode.LINEAR
                        fillColor = seriesColor
                        fillAlpha = 192
                        setDrawFilled(true)
                        setFillFormatter { dataSet, dataProvider ->
                            0f
                        }
                    })
                }
            }

            data = lineData
            graphDateRange = graphData.graphDateRange.first.toFloat() to graphData.graphDateRange.second.toFloat()

            updateChart()
        }

        private fun updateChart() {
            val dataVal = data
            val chart = currentViewHolder?.binding?.chart
            val dateRangeVal = graphDateRange
            if(chart != null && dataVal != null) {
                chart.data = dataVal
                chart.invalidate()
            }

            if(chart != null && dateRangeVal != null) {
                chart.xAxis.axisMinimum = dateRangeVal.first
                chart.xAxis.axisMaximum = dateRangeVal.second
            }
        }

        override fun onBindViewHolder(holder: GraphViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            updateChart()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClazzLogListGraphRecyclerAdapter.GraphViewHolder {
            val mBinding = FragmentClazzLogListAttendanceChartheaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false).apply {
                chart.legend.isEnabled = false
                chart.description.isEnabled = false
                chart.axisRight.setDrawLabels(false)
                val dateFormatter = DateFormat.getDateFormat(parent.context)
                chart.xAxis.setValueFormatter { value, axis ->
                    dateFormatter.format(value)
                }
                chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                chart.xAxis.labelRotationAngle = 45f
                chart.setTouchEnabled(false)
                chart.xAxis.setDrawGridLines(false)
                chart.axisRight.setDrawGridLines(false)
                chart.axisRight.setDrawAxisLine(false)
                chart.xAxis.isGranularityEnabled = true
                chart.xAxis.granularity = (1000 * 60 * 60 * 24 * 2).toFloat()
                chart.axisLeft.axisMinimum = 0f
                chart.axisLeft.axisMaximum = 100f

                chart.axisLeft.valueFormatter = PercentFormatter(DecimalFormat("###,###,##0"))
                var lastCheckedId = R.id.chip_last_week
                chipGroup.check(lastCheckedId)
                chipGroup.setOnCheckedChangeListener { group, checkedId ->
                    if(checkedId != View.NO_ID) {
                        lastCheckedId = checkedId
                        presenter?.handleClickGraphDuration(VIEW_ID_TO_NUMDAYS_MAP[checkedId] ?: 7)
                    }else {
                        chipGroup.check(lastCheckedId)
                    }

                }
            }

            return GraphViewHolder(mBinding)
        }


        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
            context = null
        }

        companion object {

            val VIEW_ID_TO_NUMDAYS_MAP = mapOf(R.id.chip_last_week to 7,
                R.id.chip_last_month to 30,
                R.id.chip_last_three_months to 90)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        autoShowFabOnAddPermission = false
        mPresenter = ClazzLogListAttendancePresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, viewLifecycleOwner, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData).also {
            mDataRecyclerViewAdapter = ClazzLogListRecyclerAdapter(it, clazzTimeZone)
        }


        graphRecyclerViewAdapter = ClazzLogListGraphRecyclerAdapter(mPresenter,
                clazzTimeZone ?: "UTC", requireContext())
        mMergeRecyclerViewAdapter = MergeAdapter(graphRecyclerViewAdapter, mDataRecyclerViewAdapter)
        mRecyclerView?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.record_attendance)
        fabManager?.icon = R.drawable.baseline_assignment_turned_in_24
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        //if(view?.id == R.id.item_createnew_layout)
            //navigateToEditEntity(null, R.id.clazzlog_edit_dest, ClazzLog::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzLogDao

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ClazzLog> = object
            : DiffUtil.ItemCallback<ClazzLog>() {
            override fun areItemsTheSame(oldItem: ClazzLog,
                                         newItem: ClazzLog): Boolean {
                return oldItem.clazzLogUid == newItem.clazzLogUid
            }

            override fun areContentsTheSame(oldItem: ClazzLog,
                                            newItem: ClazzLog): Boolean {
                return oldItem == newItem
            }
        }
    }
}