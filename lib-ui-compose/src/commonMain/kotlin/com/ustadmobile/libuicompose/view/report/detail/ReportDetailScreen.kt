package com.ustadmobile.libuicompose.view.report.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph
import com.ustadmobile.libuicompose.view.report.graphs.GraphSeries
import com.ustadmobile.libuicompose.view.report.graphs.ReportResultQueryRow
import com.ustadmobile.libuicompose.view.report.graphs.SeriesType
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.Dispatchers
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

@Composable
fun ReportDetailScreen(viewModel: ReportDetailViewModel) {
    val uiState: ReportDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        ReportDetailUiState(), Dispatchers.Main.immediate
    )
    ReportDetailScreen(
        uiState = uiState,
        onDismissDialog = viewModel::onDismissDialog,
        onShowDialog = viewModel::onShowDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    uiState: ReportDetailUiState,
    onDismissDialog: () -> Unit = { },
    onShowDialog: () -> Unit = { },
) {
    BarGraphSampleScreen(onShowDialog = onShowDialog,reportOptions = uiState.reportOptions2)
    if (uiState.dialogVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissDialog
        ) {
            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                },
                headlineContent = {

                    Text(stringResource(MR.strings.graph_data))
                },
            )

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                },
                headlineContent = {
                    Text(stringResource(MR.strings.raw_data))
                },
            )
        }
    }
}

// Example Graph
@Composable
fun BarGraphSampleScreen(
    onShowDialog: () -> Unit = { },
    reportOptions :ReportOptions2
) {
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
        ReportResultQueryRow(xAxis = "02/01/2024", yAxis = 1000000.0, subgroup = "Category N"),
    )
    val lineSeries1 = listOf(
        ReportResultQueryRow(xAxis = "female", yAxis = 20.0, subgroup = "Category A"),
        ReportResultQueryRow(xAxis = "male", yAxis = 90.0, subgroup = "Category A"),
        ReportResultQueryRow(xAxis = "female", yAxis = 20.0, subgroup = "Category B"),
        ReportResultQueryRow(xAxis = "male", yAxis = 900.0, subgroup = "Category B"),
    )
    val yAxisLabel = if (reportOptions.series.any { it.reportSeriesYAxis?.type == YAxisTypes.DURATION }) {
        "Duration"
    } else {
        "Count"
    }
    Column(modifier = Modifier.fillMaxSize()) {
        CombinedGraph(
            series = listOf(
//                GraphSeries(SeriesType.BAR, barSeries1, "Bar Series 1"),
//                GraphSeries(SeriesType.LINE, lineSeries, "Line Series 1"),
            GraphSeries(SeriesType.BAR, lineSeries1, "Line Series 2"),
            ),
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
            xAxisLabel = reportOptions.xAxis?.name?:"",
            yAxisLabel = yAxisLabel

        )
        MoreOptionsSection(
            data = listOf(
//                GraphSeries(SeriesType.BAR, barSeries1, "Bar Series 1"),
//                GraphSeries(SeriesType.LINE, lineSeries, "Line Series 1"),
                GraphSeries(SeriesType.BAR, lineSeries1, "Line Series 2"),
            ),
            onShowDialog = onShowDialog,
            modifier = Modifier.weight(0.4f)
        )
    }
}

@Composable
fun MoreOptionsSection(
    data: List<GraphSeries>,
    onShowDialog: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultScreenPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconTextButton(
                icon = Icons.Default.Share,
                text = stringResource(MR.strings.share),
                onClick = { /* Handle share */ }
            )

            IconTextButton(
                icon = Icons.Default.ImportExport,
                text = stringResource(MR.strings.export_data),
                onClick = { onShowDialog() }
            )
        }

        HorizontalDivider(thickness = 1.dp)
        LazyRow(
            modifier = Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(data) { series ->
                DataTable(data = series.data)
            }
        }
    }
}

@Composable
fun IconTextButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun DataTable(data: List<ReportResultQueryRow>) {
    val header = listOf(
        stringResource(MR.strings.x_axis),
        stringResource(MR.strings.y_axis),
        stringResource(MR.strings.sub_group)
    )
    Card(
        modifier = Modifier
            .width(IntrinsicSize.Max),
        elevation = 2.dp
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                header.forEachIndexed { index, title ->
                    Text(
                        text = title,
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (index < header.lastIndex) {
                        VerticalDivider(modifier = Modifier.height(20.dp), color = Color.Black)
                    }
                }
            }

            // Data Rows
            data.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.xAxis,
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color.Black)

                    Text(
                        text = row.yAxis.toString(),
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color.Black)

                    Text(
                        text = row.subgroup ?: "-",
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}