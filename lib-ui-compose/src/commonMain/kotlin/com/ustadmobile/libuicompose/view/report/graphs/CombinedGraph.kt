package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.formatter.GraphFormatter
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.libuicompose.view.report.detail.asString
import dev.icerock.moko.resources.compose.stringResource
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel
import kotlin.time.DurationUnit

private const val MS_IN_HOUR = 3_600_000

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    reportResult: RunReportUseCase.RunReportResult,
    modifier: Modifier = Modifier,
    xAxisFormatter: GraphFormatter<String>?,
    yAxisFormatter: GraphFormatter<Double>?
) {
    val colorMap: Map<RunReportUseCase.RunReportResult.Subgroup, Color> =
        remember(reportResult.timestamp) {
            reportResult.distinctSubgroups.mapIndexed { index, resultSubgroup ->
                resultSubgroup to Color.hsv(
                    index * 360f / reportResult.distinctSubgroups.size.coerceAtLeast(1), 1f, 0.7f
                )
            }.toMap()
        }

    //Roughly as per https://koalaplot.github.io/0.5/docs/xygraphs/bar_plots/#grouped-bars
    XYGraph(
        modifier = modifier,
        xAxisModel = remember(reportResult.timestamp) {
            CategoryAxisModel(reportResult.distinctXAxisValueSorted)
        },
        yAxisModel = rememberFloatLinearAxisModel(
            reportResult.yRange,
            minimumMajorTickIncrement = 1f
        ),
        xAxisLabels = {
            xAxisFormatter?.format(it)?.let { uiText ->
                Text(
                    text = uiText.asString(),
                    modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    maxLines = 1
                )
            }
        },
        xAxisTitle = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(reportResult.request.reportOptions.xAxis.label),
                )
            }
        },
        yAxisLabels = {
            val value = yAxisFormatter?.adjust(it.toDouble()) ?: 0.0
            val formattedText = yAxisFormatter?.format(value)
            if (formattedText != null) {
                Text(
                    text = formattedText.asString(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        },
        yAxisTitle = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (reportResult.yAxisType == YAxisTypes.DURATION) {
                        stringResource(MR.strings.duration) + getDurationUnitTitle(reportResult.yRange.endInclusive)
                    } else {
                        stringResource(MR.strings.count)
                    },
                    modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                )
            }
        }
    ) {
        /*
         * Create one barchart. For each series-subgroup combination, create a series on the barchart
         * and emit an item for each result row.
         */
        GroupedVerticalBarPlot {
            reportResult.distinctSubgroups.filter {
                it.series.reportSeriesOptions.reportSeriesVisualType == ReportSeriesVisualType.BAR_CHART
            }.forEach { resultSubgroup ->
                series(solidBar(colorMap[resultSubgroup] ?: Color.Transparent)) {
                    resultSubgroup.subgroupData.forEach { resultRow ->
                        item(resultRow.xAxis, 0f, resultRow.yAxis.toFloat())
                    }
                }
            }
        }

        /*
         * Add a line plot for each series-subgroup combination
         *  as per https://koalaplot.github.io/0.5/docs/xygraphs/line_plots/
         */
        reportResult.distinctSubgroups.filter {
            it.series.reportSeriesOptions.reportSeriesVisualType == ReportSeriesVisualType.LINE_GRAPH
        }.forEachIndexed { subgroupIndex, resultSubgroup ->
            LinePlot(
                data = resultSubgroup.subgroupData.map { row ->
                    Point(
                        row.xAxis,
                        row.yAxis.toFloat()
                    )
                },
                lineStyle = LineStyle(
                    brush = SolidColor(colorMap[resultSubgroup] ?: Color.Transparent),
                    strokeWidth = 2.dp
                ),
                symbol = {
                    Symbol(
                        shape = RoundedCornerShape(4.dp),
                        fillBrush = SolidColor(colorMap[resultSubgroup] ?: Color.Transparent)
                    )
                }
            )
        }
    }
}

@Composable
private fun getDurationUnitTitle(max: Float): String {
    return when {
        max >= MS_IN_HOUR -> " (${stringResource(MR.strings.hour_unit)})"
        else -> " (${stringResource(MR.strings.minute_unit)})"
    }
}