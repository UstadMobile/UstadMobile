package com.ustadmobile.libuicompose.view.report.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph
import com.ustadmobile.libuicompose.view.report.graphs.GraphSeries
import com.ustadmobile.libuicompose.view.report.graphs.ReportResultQueryRow
import com.ustadmobile.libuicompose.view.report.graphs.SeriesType
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ReportDetailScreen(viewModel: ReportDetailViewModel) {
    val uiState: ReportDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ReportDetailUiState(), Dispatchers.Main.immediate
    )
    ReportDetailScreen(
        uiState = uiState,
    )
}

@Composable
fun ReportDetailScreen(uiState: ReportDetailUiState) {
    BarGraphSampleScreen()
}

// Example Graph
@Composable
fun BarGraphSampleScreen() {
    CombinedGraphSample()
}

@Composable
fun CombinedGraphSample() {
    val barSeries1 = listOf(
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 5000000.0, subgroup = "Category A"),
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 4000000.0, subgroup = "Category B"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category A"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 5000000.0, subgroup = "Category B"),
        ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 4000000.0, subgroup = "Category B")
    )

    val lineSeries = listOf(
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 2000000.0, subgroup = "Category M"),
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 9000000.0, subgroup = "Category N"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 7000000.0, subgroup = "Category M"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 3000000.0, subgroup = "Category N"),
        ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 1000000.0, subgroup = "Category M"),
        ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 7000000.0, subgroup = "Category N")
    )
    val lineSeries1 = listOf(
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 2000000.0, subgroup = "Category C"),
        ReportResultQueryRow(xAxis = "01/01/2024", yAxis = 6000000.0, subgroup = "Category D"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category C"),
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 4000000.0, subgroup = "Category D"),
        ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 3000000.0, subgroup = "Category C"),
        ReportResultQueryRow(xAxis = "03/01/2024", yAxis = 9000000.0, subgroup = "Category D")
    )

    CombinedGraph(
        title = "Combined Statistics",
        series = listOf(
            GraphSeries(SeriesType.BAR, barSeries1, "Bar Series 1"),
            GraphSeries(SeriesType.LINE, lineSeries, "Line Series 1"),
            GraphSeries(SeriesType.LINE, lineSeries1, "Line Series 2"),
            ),
        modifier = Modifier.fillMaxWidth()
    )
}