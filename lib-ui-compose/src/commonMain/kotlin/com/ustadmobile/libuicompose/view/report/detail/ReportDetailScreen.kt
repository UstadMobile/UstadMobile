package com.ustadmobile.libuicompose.view.report.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.ustadmobile.core.domain.report.formatter.GraphFormatter
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.core.domain.report.utils.DefaultXAxisLabelFormatter
import com.ustadmobile.core.impl.locale.StringResourceUiText
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph
import dev.icerock.moko.resources.StringResource
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
    uiState: ReportDetailUiState
) {
    Column(Modifier.fillMaxSize()) {
        if (uiState.reportResult?.results?.isNotEmpty() == true) {
            val reportResult = uiState.reportResult
            if (reportResult != null) {
                CombinedGraph(
                    reportResult = reportResult,
                    modifier = Modifier.weight(0.6f),
                    xAxisFormatter = uiState.xAxisFormatter,
                    yAxisFormatter = uiState.yAxisFormatter
                )
                MoreOptionsSection(
                    seriesList = reportResult.resultSeries,
                    modifier = Modifier.weight(0.4f),
                    xAxisFormatter = uiState.xAxisFormatter,
                    yAxisFormatter = uiState.yAxisFormatter
                )
            }
        } else {
            EmptyDataMessage(Modifier.weight(1f))
        }
    }
}

@Composable
private fun EmptyDataMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(MR.strings.empty_data))
    }
}

@Composable
fun DataTable(
    data: List<StatementReportRow>,
    reportSeries: ReportSeries2,
    xAxisFormatter: GraphFormatter<String>?,
    yAxisFormatter: GraphFormatter<Double>?
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    // Format X-axis value
                    val xAxisValue = xAxisFormatter?.format(row.xAxis)?.asString() ?: row.xAxis
                    Text(
                        text = xAxisValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )

                    // Format Y-axis value
                    val yAxisValue = yAxisFormatter?.let {
                        val adjustedValue = it.adjust(row.yAxis)
                        it.format(adjustedValue).asString()
                    } ?: row.yAxis.toString()
                    Text(
                        text = yAxisValue,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        color = Color.LightGray,
                        thickness = 1.dp
                    )

                    // Format subgroup value
                    val subgroupValue = reportSeries.reportSeriesSubGroup?.let {
                        DefaultXAxisLabelFormatter().formatLabel(
                            value = row.subgroup,
                            it
                        )
                    }
                    val formattedSubgroupValue = when (subgroupValue) {
                        is StringResource -> stringResource(subgroupValue)
                        else -> subgroupValue?.toString() ?: ""
                    }
                    Text(
                        text = formattedSubgroupValue,
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
    seriesList: List<RunReportUseCase.RunReportResult.Series>,
    modifier: Modifier = Modifier,
    xAxisFormatter: GraphFormatter<String>?,
    yAxisFormatter: GraphFormatter<Double>?
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        item { HorizontalDivider(thickness = 1.dp) }

        items(seriesList) { series ->
            DataTable(
                data = series.data,
                reportSeries = series.reportSeriesOptions,
                xAxisFormatter = xAxisFormatter,
                yAxisFormatter = yAxisFormatter
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is StringUiText -> this.text
        is StringResourceUiText -> stringResource(this.resource)
    }
}