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
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph
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
    BarGraphSampleScreen(
        onShowDialog = onShowDialog,
        reportOptions = uiState.reportOptions2,
        statementReportRow = uiState.reportResults
    )
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

@Composable
fun BarGraphSampleScreen(
    onShowDialog: () -> Unit = { },
    reportOptions: ReportOptions2,
    statementReportRow: List<List<StatementReportRow>>
) {
    val graphSeries = remember(reportOptions, statementReportRow) {
        reportOptions.series.mapIndexed { index, reportSeries ->
            GraphSeries(
                type = when (reportSeries.reportSeriesVisualType) {
                    ReportSeriesVisualType.LINE_GRAPH -> SeriesType.LINE
                    else -> SeriesType.BAR
                },
                data = statementReportRow.getOrNull(index)?.map { statementRow ->
                    ReportResultQueryRow(
                        xAxis = statementRow.xAxis,
                        yAxis = statementRow.yAxis,
                        subgroup = statementRow.subgroup
                    )
                } ?: emptyList(),
                name = reportSeries.reportSeriesTitle
            )
        }
    }

    val yAxisLabel =
        if (reportOptions.series.any { it.reportSeriesYAxis?.type == YAxisTypes.DURATION }) {
            stringResource(MR.strings.duration_hours)
        } else {
            stringResource(MR.strings.count)
        }
    val hasAnyDuration = reportOptions.series.any {
        it.reportSeriesYAxis?.type == YAxisTypes.DURATION
    }
    if (graphSeries.isNotEmpty() && statementReportRow.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CombinedGraph(
                series = graphSeries,
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth(),
                xAxisLabel = reportOptions.xAxis,
                yAxisLabel = yAxisLabel,
                isDurationType = hasAnyDuration
            )

            MoreOptionsSection(
                data = graphSeries,
                onShowDialog = onShowDialog,
                modifier = Modifier.weight(0.4f)
            )
        }
    } else {
        androidx.compose.material.Text("empty data") // need to change
    }
}

@Composable
fun MoreOptionsSection(
    data: List<GraphSeries>,
    onShowDialog: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            HorizontalDivider(thickness = 1.dp)
        }

        items(data) { series ->
            DataTable(data = series.data)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DataTable(data: List<ReportResultQueryRow>) {
    val header = listOf(
        stringResource(MR.strings.x_axis),
        stringResource(MR.strings.y_axis),
        stringResource(MR.strings.subgroup_by)
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
                    )
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color.Black)

                    Text(
                        text = row.yAxis.toString(),
                        modifier = Modifier.weight(0.5f),
                    )
                    VerticalDivider(modifier = Modifier.height(20.dp), color = Color.Black)

                    Text(
                        text = row.subgroup ?: "-",
                        modifier = Modifier.weight(0.5f),
                    )
                }
            }
        }
    }
}