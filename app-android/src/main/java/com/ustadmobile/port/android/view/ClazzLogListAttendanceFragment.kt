package com.ustadmobile.port.android.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.composethemeadapter.MdcTheme
import com.soywiz.klock.DateTime
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzLogListAttendanceChartheaderBinding
import com.toughra.ustadmobile.databinding.ItemClazzLogAttendanceListBinding
import com.ustadmobile.core.controller.ClazzLogListAttendancePresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.view.ClazzLogListAttendanceView
import com.ustadmobile.core.viewmodel.AttendanceGraphData
import com.ustadmobile.core.viewmodel.ClazzLogListAttendanceUiState
import com.ustadmobile.door.lifecycle.MutableLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.port.android.util.compose.rememberFormattedDateTime
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter
import com.ustadmobile.port.android.view.util.SingleItemRecyclerViewAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import java.text.DecimalFormat
import java.util.*


class ClazzLogListAttendanceFragment(): UstadListViewFragment<ClazzLog, ClazzLog>(),
        ClazzLogListAttendanceView, MessageIdSpinner.OnMessageIdOptionSelectedListener, View.OnClickListener,
        BottomSheetOptionSelectedListener{

    private var mPresenter: ClazzLogListAttendancePresenter? = null

    override val listPresenter: UstadListPresenter<*, in ClazzLog>?
        get() = mPresenter

    override var autoMergeRecyclerViewAdapter: Boolean = false

    override var clazzTimeZone: String?
        get() = (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone
        set(value) {
            (mDataRecyclerViewAdapter as? ClazzLogListRecyclerAdapter)?.clazzTimeZone = value
        }

    override var graphData: MutableLiveData<ClazzLogListAttendancePresenter.AttendanceGraphData>? = null
        set(value) {
            val observer = graphRecyclerViewAdapter ?: return
            field?.removeObserver(observer)
            field = value
            field?.observe(viewLifecycleOwner, observer)
        }


    override var recordAttendanceOptions: List<ClazzLogListAttendancePresenter.RecordAttendanceOption>? = null
        set(value) {
            fabManager?.visible = !value.isNullOrEmpty()
            field = value
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

        private var decimalFormat = DecimalFormat("###,###,##0")

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
                chart.xAxis.valueFormatter = object: ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return dateFormatter.format(value)
                    }
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
                chart.axisLeft.valueFormatter = object: ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return "${decimalFormat.format(value)}%"
                    }
                }
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
                this, di, viewLifecycleOwner).withViewLifecycle().also {
            mDataRecyclerViewAdapter = ClazzLogListRecyclerAdapter(it, clazzTimeZone)
        }


        graphRecyclerViewAdapter = ClazzLogListGraphRecyclerAdapter(mPresenter,
                clazzTimeZone ?: "UTC", requireContext())
        mMergeRecyclerViewAdapter = ConcatAdapter(graphRecyclerViewAdapter, mDataRecyclerViewAdapter)
        mRecyclerView?.adapter = mMergeRecyclerViewAdapter

        return view
    }

    fun ClazzLogListAttendancePresenter.RecordAttendanceOption.toBottomSheetOption(): BottomSheetOption {
        val systemImpl : UstadMobileSystemImpl = direct.instance()
        return BottomSheetOption(RECORD_ATTENDANCE_OPTIONS_ICON[this] ?: 0,
            systemImpl.getString(this.messageId, requireContext()), this.commandId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getString(R.string.record_attendance)
        fabManager?.icon = R.drawable.baseline_assignment_turned_in_24
        fabManager?.onClickListener = {
            val bottomSheet = OptionsBottomSheetFragment(recordAttendanceOptions?.map {
                it.toBottomSheetOption()
            } ?: listOf(), this)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(view: View?) {
        //if(view?.id == R.id.item_createnew_layout)
            //navigateToEditEntity(null, R.id.clazzlog_edit_dest, ClazzLog::class.java)
    }

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        mPresenter?.handleClickRecordAttendance(
            ClazzLogListAttendancePresenter.RecordAttendanceOption.values().first {
                it.commandId == optionSelected.optionCode
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.clazzLogDao

    companion object {

        val RECORD_ATTENDANCE_OPTIONS_ICON = mapOf(
                ClazzLogListAttendancePresenter.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE
                        to R.drawable.ic_calendar_today_24px_,
                ClazzLogListAttendancePresenter.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE
                        to R.drawable.ic_add_black_24dp
        )

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

private val DECIMAL_FORMAT = DecimalFormat("###,###,##0")

private fun LineChart.updateLineData(graphData: AttendanceGraphData) {
    val lineData = LineData().apply {
        listOf(graphData.percentageAttendedSeries, graphData.percentageLateSeries).forEachIndexed { index, list ->
            val colorId = if(index == 0) R.color.successColor else R.color.secondaryColor
            val seriesColor = context?.let { ContextCompat.getColor(context, colorId) } ?: Color.BLACK
            addDataSet(LineDataSet(list.map { Entry(it.first.toFloat(), it.second * 100) },
                context.getString(R.string.attendance)).apply {
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
    invalidate()
    xAxis.axisMinimum = graphData.graphDateRange.first.toFloat()
    xAxis.axisMaximum = graphData.graphDateRange.second.toFloat()
}

@Composable
private fun ClazzLogListAttendanceScreen(
    uiState: ClazzLogListAttendanceUiState = ClazzLogListAttendanceUiState(),
    onClickClazz: (ClazzLog) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

        item {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                factory = {  context ->
                    //As per onCreateViewHolder of chart recycler adapter
                    LineChart(context).apply {
                        legend.isEnabled = false
                        description.isEnabled = false
                        axisRight.setDrawLabels(false)
                        val dateFormatter = DateFormat.getDateFormat(context)
                        xAxis.valueFormatter = object: ValueFormatter(){
                            override fun getFormattedValue(value: Float): String {
                                return dateFormatter.format(value)
                            }
                        }
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.labelRotationAngle = 45f
                        setTouchEnabled(false)
                        xAxis.setDrawGridLines(false)
                        axisRight.setDrawGridLines(false)
                        axisRight.setDrawAxisLine(false)
                        xAxis.isGranularityEnabled = true
                        xAxis.granularity = (1000 * 60 * 60 * 24 * 2).toFloat()
                        axisLeft.axisMinimum = 0f
                        axisLeft.axisMaximum = 100f
                        axisLeft.valueFormatter = object: ValueFormatter(){
                            override fun getFormattedValue(value: Float): String {
                                return "${DECIMAL_FORMAT.format(value)}%"
                            }
                        }

                        uiState.graphData?.also { updateLineData(it) }
                    }

                },
                update = {
                    //As per onBind of chart recycler adapter
                    val graphData = uiState.graphData ?: return@AndroidView
                    if(graphData.percentageAttendedSeries.size < 2)
                        return@AndroidView

                    it.updateLineData(graphData)

                }
            )
        }

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.viewIdToNumDays,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        items(
            items = uiState.clazzLogsList,
            key = { clazzLog -> clazzLog.clazzLogUid }
        ){ clazzLog ->
            ClazzLogListItem(
                clazzLog = clazzLog,
                onClick = onClickClazz
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzLogListItem(
    clazzLog: ClazzLog,
    onClick: (ClazzLog) -> Unit,
){

    val dateTime = rememberFormattedDateTime(
        timeInMillis = clazzLog.logDate,
        timeZoneId = TimeZone.getDefault().id
    )

    val attendancePairList: List<Pair<Int, Int>> = listOf(
            Pair(clazzLog.clazzLogNumPresent, R.color.successColor) ,
            Pair(clazzLog.clazzLogNumPartial, R.color.secondaryColor) ,
            Pair(clazzLog.clazzLogNumAbsent, R.color.errorColor) ,
    )

    ListItem(
        modifier = Modifier.clickable {
            onClick(clazzLog)
        },
        icon = {
            Icon(
                Icons.Outlined.CalendarToday,
                contentDescription = ""
            )
        },
        text = { Text(dateTime) },
        secondaryText = {
            Column {
                Row {
                    attendancePairList.forEach { pair ->
                        Box(modifier = Modifier
                            .weight((pair.first).toFloat())
                            .height(6.dp)
                            .background(color = colorResource(id = pair.second))
                        )
                    }
                }
                Text(text = stringResource(
                    id = R.string.three_num_items_with_name_with_comma,
                    clazzLog.clazzLogNumPresent,
                    stringResource(R.string.present),
                    clazzLog.clazzLogNumPartial,
                    stringResource(R.string.partial),
                    clazzLog.clazzLogNumAbsent,
                    stringResource(R.string.absent)
                ))
            }
        }
    )
}

@Composable
@Preview
fun ClazzLogListAttendanceScreenPreview() {
    val uiStateVal = ClazzLogListAttendanceUiState(
        clazzLogsList = listOf(
            ClazzLog().apply {
                clazzLogUid = 1
                clazzLogNumPresent = 40
                clazzLogNumPartial = 15
                clazzLogNumAbsent = 10
            },
            ClazzLog().apply {
                clazzLogUid = 2
                clazzLogNumPresent = 40
                clazzLogNumPartial = 30
                clazzLogNumAbsent = 30
                logDate = 1673683347000
            },
            ClazzLog().apply {
                clazzLogUid = 3
                clazzLogNumPresent = 70
                clazzLogNumPartial = 20
                clazzLogNumAbsent = 2
                logDate = 1673683347000
            }
        ),
        graphData = AttendanceGraphData(
             percentageAttendedSeries = listOf(
                 Pair(1674743891000, .80f),
                 Pair(1674830291000, .70f),
                 Pair(1674916691000, .50f),
                 Pair(1675003091000, .40f),
                 Pair(1675089491000, .15f),
             ),
            percentageLateSeries = listOf(
                Pair(1674743891000, .15f),
                Pair(1674830291000, .20f),
                Pair(1674916691000, .10f),
                Pair(1675003091000, .30f),
                Pair(1675089491000, .60f),
            ),
            graphDateRange = Pair(1674743891000, 1675089491000),
        )
    )
    MdcTheme {
        ClazzLogListAttendanceScreen(uiStateVal)
    }
}