package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import dev.icerock.moko.resources.compose.stringResource
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel

private const val MS_IN_HOUR = 3_600_000
private const val MS_IN_MINUTE = 60_000
private const val MS_IN_SECOND = 1_000

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    reportResult: RunReportUseCase.RunReportResult,
    modifier: Modifier = Modifier,
) {
    // Generate colors for each subgroup
    val colors = remember(reportResult.timestamp) {
        reportResult.distinctSubgroups.mapIndexed { index, s ->
            Color.hsv(index * 360f / reportResult.distinctSubgroups.size.coerceAtLeast(1), 1f, 0.7f)
        }
    }

    //Roughly as per https://koalaplot.github.io/0.5/docs/xygraphs/bar_plots/#grouped-bars
    XYGraph(
        modifier = modifier,
        xAxisModel = remember(reportResult.timestamp) {
            CategoryAxisModel(reportResult.distinctXAxisValueSorted)
        },
        yAxisModel = rememberFloatLinearAxisModel(reportResult.yRange, minimumMajorTickIncrement = 1f),
        xAxisLabels = {
            Text(
                text = it,
                modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        xAxisTitle = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(reportResult.request.reportOptions.xAxis.label),
                    modifier = Modifier.padding(bottom = KoalaPlotTheme.sizes.gap)
                )
            }
        },
        yAxisLabels = {
            Text(
                text = if (reportResult.yAxisType == YAxisTypes.DURATION) {
                    formatDurationLabel(it)
                } else {
                    it.toInt().toString()
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        yAxisTitle = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (reportResult.yAxisType == YAxisTypes.DURATION) {
                        val unit = getDurationUnitTittle(reportResult.yRange.endInclusive)
                        stringResource(MR.strings.duration) + unit
                    } else {
                        stringResource(MR.strings.count)
                    },

                    modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                )
            }
        }
    ) {
        GroupedVerticalBarPlot {
            reportResult.distinctSubgroups.forEachIndexed { subgroupIndex, subgroup ->
                series(solidBar(colors[subgroupIndex])) {
                    // For each x value, find matching data points
                    reportResult.distinctXAxisValueSorted.forEach { xValue ->
                        reportResult.barSeries.firstNotNullOfOrNull { seriesItem ->
                            seriesItem.data.firstOrNull { it.xAxis == xValue && it.subgroup == subgroup }
                        }?.also { dataPoint ->
                            // There may or may not be a bar for each xAxis/subgroup combination.
                            // Show a bar only if there is data for this combination
                            item(xValue, 0f, dataPoint.yAxis.toFloat())
                        }
                    }
                }
            }
        }

        //For each line series, add a line plot for each subgroup
        reportResult.lineSeries.forEach { series ->
            series.data.groupBy { it.subgroup }.filter { dataRows ->
                dataRows.value.isNotEmpty()
            }.forEach { (subgroup, statementReportRows) ->
                val subgroupIndex = reportResult.distinctSubgroups.indexOf(subgroup)

                LinePlot(
                    data = statementReportRows.map { row -> Point(row.xAxis, row.yAxis.toFloat()) },
                    lineStyle = LineStyle(
                        brush = SolidColor(colors[subgroupIndex]),
                        strokeWidth = 2.dp
                    ),
                    symbol = {
                        Symbol(
                            shape = RoundedCornerShape(4.dp),
                            fillBrush = SolidColor(colors[subgroupIndex])
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun convertDuration(ms: Float): Pair<Int, String> {
    return when {
        ms >= MS_IN_HOUR -> ((ms / MS_IN_HOUR).toInt() to stringResource(MR.strings.hour_unit))
        ms >= MS_IN_MINUTE -> ((ms / MS_IN_MINUTE).toInt() to stringResource(MR.strings.minute_unit))
        ms >= MS_IN_SECOND -> ((ms / MS_IN_SECOND).toInt() to stringResource(MR.strings.second_unit))
        else -> (ms.toInt() to stringResource(MR.strings.millisecond_unit))
    }
}
@Composable
fun formatDurationLabel(ms: Float): String {
    val (value, _) = convertDuration(ms)
    return "$value"
}
@Composable
fun getDurationUnitTittle(max: Float): String {
    val (_, unit) = convertDuration(max)
    return " ($unit)"
}