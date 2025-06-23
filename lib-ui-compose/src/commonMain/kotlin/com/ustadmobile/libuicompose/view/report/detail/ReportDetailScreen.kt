package com.ustadmobile.libuicompose.view.report.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.utils.ReportFormatter
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
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
    )
}

@Composable
fun ReportDetailScreen(
    uiState: ReportDetailUiState,
) {
    BarGraphSection(
        reportOptions = uiState.reportOptions2,
        statementReportRow = uiState.reportResults
    )
}

@Composable
fun BarGraphSection(
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

    val yAxisLabel = if (reportOptions.series.any {
            it.reportSeriesYAxis.type == YAxisTypes.DURATION
        }) {
        stringResource(YAxisTypes.DURATION.label) // Get from enum
    } else {
        stringResource(YAxisTypes.COUNT.label) // Get from enum
    }

    val hasAnyDuration = reportOptions.series.any {
        it.reportSeriesYAxis.type == YAxisTypes.DURATION
    }
    if (graphSeries.isNotEmpty() && statementReportRow.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CombinedGraph(
                series = graphSeries,
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth(),
                yAxisLabel = yAxisLabel,
                isDurationType = hasAnyDuration,
                reportOptions = reportOptions
                )

            MoreOptionsSection(
                data = graphSeries,
                modifier = Modifier.weight(0.4f),
                reportOptions = reportOptions
            )
        }
    } else {
        Text(  stringResource(MR.strings.empty_data))
    }
}

@Composable
fun DataTable(
    data: List<ReportResultQueryRow>,
    reportSeries: ReportSeries2,
    xAxisType: ReportXAxis?
) {
    val subgroupName = reportSeries.reportSeriesSubGroup?.label?.let { stringResource(it) }
    val subgroupByStr = stringResource(MR.strings.subgroup_by)

    val subgroupLabel = remember(subgroupName) {
        if (subgroupName != null) {
            "$subgroupByStr - $subgroupName"
        } else {
            subgroupByStr
        }
    }

    val header = listOf(
        stringResource(MR.strings.x_axis),
        stringResource(MR.strings.y_axis),
        subgroupLabel
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                header.forEachIndexed { index, title ->
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (index < header.lastIndex) {
                        VerticalDivider(
                            modifier = Modifier.height(24.dp),
                            color = Color.DarkGray,
                            thickness = 1.dp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)

            data.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = xAxisType?.let {
                            when (it) {
                                ReportXAxis.GENDER -> getGenderLabel(row.xAxis)
                                ReportXAxis.CLASS -> row.xAxis
                                else -> ReportFormatter.formatDateForReport(
                                    row.xAxis,
                                    it,
                                )
                            }
                        } ?: row.xAxis,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )
                    Text(
                        text = row.yAxis.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )
                    Text(
                        text =reportSeries.reportSeriesSubGroup?.let {
                            when (it) {
                                ReportXAxis.GENDER -> getGenderLabel(row.subgroup)
                                ReportXAxis.CLASS -> row.subgroup
                                else -> ReportFormatter.formatDateForReport(
                                    row.subgroup ?: "",
                                    it,
                                )
                            }
                        } ?: row.subgroup ?: "",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                HorizontalDivider(color = Color.LightGray)
            }
        }
    }
}

@Composable
fun MoreOptionsSection(
    reportOptions: ReportOptions2,
    data: List<GraphSeries>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item { HorizontalDivider(thickness = 1.dp) }

        items(data) { series ->
            // Get the corresponding report series by index
            val reportSeries = reportOptions.series.getOrNull(data.indexOf(series))

            reportSeries?.let {
                DataTable(
                    data = series.data,
                    reportSeries = it,
                    xAxisType = reportOptions.xAxis
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun getGenderLabel(rawValue: String?): String {
    return when (rawValue) {
        GENDER_FEMALE.toString() -> stringResource(MR.strings.female)
        GENDER_MALE.toString() -> stringResource(MR.strings.male)

        else -> rawValue?.toString() ?: ""
    }
}