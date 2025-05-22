package com.ustadmobile.libuicompose.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.ustadmobile.libuicompose.components.UstadLazyColumn
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

    UstadLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        ustadPagedItems(
            pagingItems = doorRepoPager.lazyPagingItems,
            key = { it.reportUid },
        ) { report ->
            ReportListItem(
                report = report,
                viewModel = viewModel,
                onItemClick = onListItemClick,
                onRemove = onRemoveReport
            )
        }
    }
}

@Composable
private fun ReportListItem(
    report: Report?,
    viewModel: ReportListViewModel,
    onItemClick: (Report) -> Unit,
    onRemove: (Long) -> Unit
) {
    if (report == null) return

    // Collect report data
    val reportDataFlow = remember(report.reportUid) {
        viewModel.runReport(report)
    }
    val reportDataResult by reportDataFlow.collectAsState(
        initial = ReportDataResult(null, emptyList())
    )

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

    ListItem(
        modifier = Modifier
            .clickable { onItemClick(report) }
            .fillMaxWidth(),
        headlineContent = {
            Text(
                report.reportTitle ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(100.dp, 100.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    reportDataResult.options == null ->
                        CircularProgressIndicator(Modifier.size(24.dp))

                    reportDataResult.data.isEmpty() ->
                        Text("No data", style = MaterialTheme.typography.bodySmall)

                    else -> CombinedGraph(
                        series = graphSeries,
                        xAxisLabel = reportDataResult.options?.xAxis ?: ReportXAxis.GENDER,
                        yAxisLabel = stringResource(MR.strings.activity),
                        isDurationType = reportDataResult.options?.series?.any {
                            it.reportSeriesYAxis?.type == YAxisTypes.DURATION
                        } ?: false,
                        compactMode = true,
                    )
                }
            }
        },
        trailingContent = {
            Box(
                modifier = Modifier
                    .size(80.dp, 80.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(MR.strings.delete),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onRemove(report.reportUid) }
                )
            }
        }
    )
}