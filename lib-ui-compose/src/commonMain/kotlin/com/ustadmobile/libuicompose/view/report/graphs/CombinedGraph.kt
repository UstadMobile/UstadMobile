package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import dev.icerock.moko.resources.compose.stringResource
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel

@Composable
fun CombinedGraph(
    reportResult: RunReportUseCase.RunReportResult,
    modifier: Modifier = Modifier,
) {
    val series = reportResult.resultSeries
    val xAxisLabel = reportResult.request.reportOptions.xAxis

    // Get all distinct x-axis values
    val xValues = remember(series) {
        reportResult.distinctXAxisValueSorted
    }

    // Format the x-axis values for display
    val formattedXValues = remember(xValues) {
        xValues.map { it.toString() }
    }

    // Get all distinct subgroups
    val subgroups = remember(series) {
        series.flatMap { it.data.map { row -> row.subgroup } }.distinct()
    }

    // Generate colors for each subgroup
    val colors = remember(subgroups) {
        List(subgroups.size) { index ->
            Color.hsv(index * 360f / subgroups.size.coerceAtLeast(1), 1f, 0.7f)
        }
    }

    // Calculate max value for Y-axis scaling with fallback
    val yRange = remember(series) {
        val maxValue = series.flatMap { it.data.map { row -> row.yAxis } }.maxOrNull() ?: 0.0
        if (maxValue > 0) 0f..(maxValue * 1.1).toFloat() else 0f..1f
    }

    val isDurationType = series.any {
        it.reportSeriesOptions.reportSeriesYAxis.type == YAxisTypes.DURATION
    }
    val yAxisLabel = stringResource(
        if (isDurationType) MR.strings.duration_hours else MR.strings.count
    )

    XYGraph(
        modifier = modifier,
        xAxisModel = CategoryAxisModel(formattedXValues),
        yAxisModel = rememberFloatLinearAxisModel(yRange),
        xAxisLabels = {
            Text(
                it.toString(),
                modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        xAxisTitle = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(xAxisLabel.label),
                    modifier = Modifier.padding(bottom = KoalaPlotTheme.sizes.gap)
                )
            }
        },
        yAxisLabels = {
            Text(
                it.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        yAxisTitle = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    yAxisLabel,
                    modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                        .padding(bottom = KoalaPlotTheme.sizes.gap)
                )
            }
        }
    ) {
        GroupedVerticalBarPlot {
            subgroups.forEachIndexed { subgroupIndex, subgroup ->
                series(solidBar(colors[subgroupIndex])) {
                    // For each x value, find matching data points
                    xValues.forEach { xValue ->
                        val dataPoint = series.firstNotNullOfOrNull { seriesItem ->
                            seriesItem.data.firstOrNull { it.xAxis == xValue && it.subgroup == subgroup }
                        }
                        item(xValue, 0f, dataPoint?.yAxis?.toFloat() ?: 0f)
                    }
                }
            }
        }
    }
}