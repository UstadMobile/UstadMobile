package com.ustadmobile.libuicompose.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.report.list.ReportDataResult
import com.ustadmobile.core.viewmodel.report.list.ReportListUiState
import com.ustadmobile.core.viewmodel.report.list.ReportListViewModel
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.libuicompose.components.ustadPagedItems
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel
) {
    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())

    ReportListScreen(
        uiState = uiState,
        onListItemClick = viewModel::onClickEntry,
        listRefreshCommand = viewModel.refreshCommandFlow,
        onRemoveReport = viewModel::onRemoveReport,
        viewModel = viewModel
    )
}

@Composable
fun ReportListScreen(
    uiState: ReportListUiState,
    onListItemClick: (Report) -> Unit,
    listRefreshCommand: Flow<RefreshCommand> = emptyFlow(),
    onRemoveReport: (Long) -> Unit,
    viewModel: ReportListViewModel
) {
    val doorRepoPager = rememberDoorRepositoryPager(
        pagingSourceFactory = uiState.reportList,
        refreshCommandFlow = listRefreshCommand,
    )
    val pagingItems = doorRepoPager.lazyPagingItems

    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp), // Minimum card width 200dp
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        ustadPagedItems(
            pagingItems = pagingItems,
            key = { it.reportUid },
        ) { report ->
            ReportGridCard(
                report = report,
                viewModel = viewModel,
                onItemClick = onListItemClick,
                onRemove = onRemoveReport
            )
        }
    }
}

@Composable
private fun ReportGridCard(
    report: Report?,
    viewModel: ReportListViewModel,
    onItemClick: (Report) -> Unit,
    onRemove: (Long) -> Unit
) {
    if (report == null) return

    val reportDataFlow = remember(report.reportUid) {
        viewModel.runReport(report)
    }
    val reportDataResult by reportDataFlow.collectAsState(
        initial = ReportDataResult(null, emptyList())
    )

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onItemClick(report) }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)),
         elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                // Title above the chart
                Text(
                    report.reportTitle ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Chart content
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        reportDataResult.options == null ->
                            CircularProgressIndicator(Modifier.size(32.dp))

                        reportDataResult.data.isEmpty() ->
                            Text(stringResource(MR.strings.No_data_available), style = MaterialTheme.typography.bodyMedium)

                        else -> {
                            val graphSeries = remember(reportDataResult) {
                                reportDataResult.options?.series?.mapIndexed { index, reportSeries ->
                                    GraphSeries(
                                        type = when (reportSeries.reportSeriesVisualType) {
                                            ReportSeriesVisualType.LINE_GRAPH -> SeriesType.LINE
                                            else -> SeriesType.BAR
                                        },
                                        data = reportDataResult.data.getOrNull(index)?.map { statementRow ->
                                            ReportResultQueryRow(
                                                xAxis = statementRow.xAxis,
                                                yAxis = statementRow.yAxis,
                                                subgroup = statementRow.subgroup
                                            )
                                        } ?: emptyList(),
                                        name = reportSeries.reportSeriesTitle
                                    )
                                } ?: emptyList()
                            }
                            val yAxisLabel =
                                if (reportDataResult.options?.series?.any { it.reportSeriesYAxis?.type == YAxisTypes.DURATION } == true) {
                                    stringResource(MR.strings.duration_hours)
                                } else {
                                    stringResource(MR.strings.count)
                                }

                            CombinedGraph(
                                series = graphSeries,
                                xAxisLabel = reportDataResult.options?.xAxis ?: ReportXAxis.GENDER,
                                yAxisLabel = yAxisLabel,
                                isDurationType = reportDataResult.options?.series?.any {
                                    it.reportSeriesYAxis.type == YAxisTypes.DURATION
                                } ?: false,
                                compactMode = true,
                                modifier = Modifier.fillMaxSize()
                                    .background(Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Delete icon positioned in top-right corner
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(MR.strings.delete),
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp)
                    .clickable { onRemove(report.reportUid) }
                    .align(Alignment.TopEnd)
            )
        }
    }
}