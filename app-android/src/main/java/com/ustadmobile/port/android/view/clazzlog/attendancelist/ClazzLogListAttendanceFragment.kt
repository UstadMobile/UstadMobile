package com.ustadmobile.port.android.view.clazzlog.attendancelist

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.AttendanceGraphData
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import com.ustadmobile.port.android.view.composable.UstadListFilterChipsHeader
import org.kodein.di.direct
import org.kodein.di.instance
import java.text.DecimalFormat
import java.util.*
import com.ustadmobile.core.schedule.totalAttendeeStatusRecorded
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import com.ustadmobile.port.android.view.BottomSheetOption
import com.ustadmobile.port.android.view.OptionsBottomSheetFragment
import com.ustadmobile.port.android.view.UstadBaseMvvmFragment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.max
import com.ustadmobile.core.R as CR

class ClazzLogListAttendanceFragment(): UstadBaseMvvmFragment() {


    fun ClazzLogListAttendanceViewModel.RecordAttendanceOption.toBottomSheetOption(): BottomSheetOption {
        val systemImpl : UstadMobileSystemImpl = direct.instance()
        return BottomSheetOption(
            RECORD_ATTENDANCE_OPTIONS_ICON[this] ?: 0,
            systemImpl.getString(this.stringResource), this.commandId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )

            setContent {
                MdcTheme {

                }
            }
        }
    }


    private fun onClickFab() {
//        lifecycleScope.launch {
//            val uiState = viewModel.uiState.first()
//            if(uiState.recordAttendanceOptions.size == 1) {
//                viewModel.onClickRecordAttendance(
//                    uiState.recordAttendanceOptions.first()
//                )
//            }else {
//                OptionsBottomSheetFragment(
//                    optionsList = uiState.recordAttendanceOptions.map {
//                        it.toBottomSheetOption()
//                    },
//                    onOptionSelected = {option ->
//                        viewModel.onClickRecordAttendance(
//                            ClazzLogListAttendanceViewModel.RecordAttendanceOption.forCommand(
//                                option.optionCode
//                            )
//                        )
//                    }
//                ).show(requireActivity().supportFragmentManager, "attendance_options")
//            }
//        }
    }



    companion object {

        val RECORD_ATTENDANCE_OPTIONS_ICON = mapOf(
            ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_MOST_RECENT_SCHEDULE
                        to R.drawable.ic_calendar_today_24px_,
            ClazzLogListAttendanceViewModel.RecordAttendanceOption.RECORD_ATTENDANCE_NEW_SCHEDULE
                        to R.drawable.ic_add_black_24dp
        )

    }
}

private val DECIMAL_FORMAT = DecimalFormat("###,###,##0")

private fun LineChart.updateLineData(graphData: AttendanceGraphData) {
    val lineData = LineData().apply {
        listOf(graphData.percentageAttendedSeries, graphData.percentageLateSeries).forEachIndexed { index, list ->
            val colorId = if(index == 0) R.color.successColor else R.color.secondaryColor
            val seriesColor = context?.let { ContextCompat.getColor(context, colorId) } ?: Color.BLACK
            addDataSet(LineDataSet(list.map { Entry(it.first.toFloat(), it.second * 100) },
                context.getString(CR.string.attendance)).apply {
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
fun ClazzLogListAttendanceScreen(
    viewModel: ClazzLogListAttendanceViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ClazzLogListAttendanceUiState())

    ClazzLogListAttendanceScreen(
        uiState = uiState,
        onClickClazz = viewModel::onClickEntry,
    )
}

@Composable
private fun ClazzLogListAttendanceScreen(
    uiState: ClazzLogListAttendanceUiState = ClazzLogListAttendanceUiState(),
    onClickClazz: (ClazzLog) -> Unit = {},
    onClickFilterChip: (MessageIdOption2) -> Unit = {},
) {

    val pager = remember(uiState.clazzLogsList) {
        Pager(
            pagingSourceFactory = uiState.clazzLogsList,
            config = PagingConfig(pageSize = 50)
        )
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        /* Graph will be brought back later
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
         */

        item {
            UstadListFilterChipsHeader(
                filterOptions = uiState.viewIdToNumDays,
                selectedChipId = uiState.selectedChipId,
                enabled = uiState.fieldsEnabled,
                onClickFilterChip = onClickFilterChip,
            )
        }

        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey{ clazzLog -> clazzLog.clazzLogUid }
        ){ index ->
            ClazzLogListItem(
                clazzLog = lazyPagingItems[index],
                timeZoneId = uiState.timeZoneId,
                onClick = onClickClazz
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ClazzLogListItem(
    clazzLog: ClazzLog?,
    timeZoneId: String,
    onClick: (ClazzLog) -> Unit,
){

    val dateTime = rememberFormattedDateTime(
        timeInMillis = clazzLog?.logDate ?: 0,
        timeZoneId = timeZoneId,
    )

    ListItem(
        modifier = Modifier.clickable {
            clazzLog?.also(onClick)
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
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumPresent ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        colorId = R.color.successColor,
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumPartial ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        colorId = R.color.secondaryColor,
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumAbsent ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        colorId = R.color.errorColor,
                    )
                }

                Text(text = stringResource(
                    id = CR.string.three_num_items_with_name_with_comma,
                    clazzLog?.clazzLogNumPresent ?: 0,
                    stringResource(CR.string.present),
                    clazzLog?.clazzLogNumPartial ?: 0,
                    stringResource(CR.string.partial),
                    clazzLog?.clazzLogNumAbsent ?: 0,
                    stringResource(CR.string.absent)
                ))
            }
        }
    )
}

@Composable
private fun RowScope.ClazzLogListItemAttendanceStatusBox(
    numerator: Int,
    denominator: Int,
    colorId: Int,
) {
    if(numerator > 0) {
        Box(modifier = Modifier
            .weight((numerator * 100).toFloat() / max(denominator, 1))
            .height(6.dp)
            .background(color = colorResource(id = colorId))
        )
    }
}

@Composable
@Preview
fun ClazzLogListAttendanceScreenPreview() {
    val uiStateVal = ClazzLogListAttendanceUiState(
        clazzLogsList = {
            ListPagingSource(listOf(
                ClazzLog().apply {
                    clazzLogUid = 1
                    clazzLogNumPresent = 40
                    clazzLogNumPartial = 15
                    clazzLogNumAbsent = 10
                    logDate = 1675089491000
                },
                ClazzLog().apply {
                    clazzLogUid = 2
                    clazzLogNumPresent = 40
                    clazzLogNumPartial = 30
                    clazzLogNumAbsent = 30
                    logDate = 1675003091000
                },
                ClazzLog().apply {
                    clazzLogUid = 3
                    clazzLogNumPresent = 70
                    clazzLogNumPartial = 20
                    clazzLogNumAbsent = 2
                    logDate = 1674916691000
                }
            ))
        },
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