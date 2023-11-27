package com.ustadmobile.libuicompose.view.clazzlog.attendancelist

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import java.text.DecimalFormat
import java.util.*
import com.ustadmobile.core.schedule.totalAttendeeStatusRecorded
import com.ustadmobile.core.viewmodel.clazzlog.attendancelist.ClazzLogListAttendanceViewModel
import kotlin.math.max
import com.ustadmobile.core.MR
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import dev.icerock.moko.resources.compose.colorResource
import dev.icerock.moko.resources.compose.stringResource
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems

private val DECIMAL_FORMAT = DecimalFormat("###,###,##0")

//private fun LineChart.updateLineData(graphData: AttendanceGraphData) {
//    val lineData = LineData().apply {
//        listOf(graphData.percentageAttendedSeries, graphData.percentageLateSeries).forEachIndexed { index, list ->
//            val colorId = if(index == 0) R.color.successColor else R.color.secondaryColor
//            val seriesColor = context?.let { ContextCompat.getColor(context, colorId) } ?: Color.BLACK
//            addDataSet(LineDataSet(list.map { Entry(it.first.toFloat(), it.second * 100) },
//                context.getString(CR.string.attendance)).apply {
//                color = seriesColor
//                valueTextColor = Color.BLACK
//                lineWidth = 1f
//                setDrawValues(false)
//                setDrawCircles(false)
//                mode = LineDataSet.Mode.LINEAR
//                fillColor = seriesColor
//                fillAlpha = 192
//                setDrawFilled(true)
//                setFillFormatter { dataSet, dataProvider ->
//                    0f
//                }
//            })
//        }
//    }
//
//
//    data = lineData
//    invalidate()
//    xAxis.axisMinimum = graphData.graphDateRange.first.toFloat()
//    xAxis.axisMaximum = graphData.graphDateRange.second.toFloat()
//}

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
fun ClazzLogListAttendanceScreen(
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

        ustadPagedItems(
            pagingItems = lazyPagingItems,
            key = { clazzLog -> clazzLog.clazzLogUid }
        ){ clazzLog ->
            ClazzLogListItem(
                clazzLog = clazzLog,
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
                        color = colorResource(MR.colors.success),
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumPartial ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        color = MaterialTheme.colors.secondary,
                    )
                    ClazzLogListItemAttendanceStatusBox(
                        numerator = clazzLog?.clazzLogNumAbsent ?: 0,
                        denominator = clazzLog?.totalAttendeeStatusRecorded ?: 1,
                        color = MaterialTheme.colors.error,
                    )
                }

                Text(text = stringResource(
                    MR.strings.three_num_items_with_name_with_comma,
                    clazzLog?.clazzLogNumPresent ?: 0,
                    stringResource(MR.strings.present),
                    clazzLog?.clazzLogNumPartial ?: 0,
                    stringResource(MR.strings.partial),
                    clazzLog?.clazzLogNumAbsent ?: 0,
                    stringResource(MR.strings.absent)
                ))
            }
        }
    )
}

@Composable
private fun RowScope.ClazzLogListItemAttendanceStatusBox(
    numerator: Int,
    denominator: Int,
    color: Color,
) {
    if(numerator > 0) {
        Box(modifier = Modifier
            .weight((numerator * 100).toFloat() / max(denominator, 1))
            .height(6.dp)
            .background(color = color)
        )
    }
}