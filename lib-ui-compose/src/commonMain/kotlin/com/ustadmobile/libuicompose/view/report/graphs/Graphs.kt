package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import dev.icerock.moko.resources.compose.stringResource
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotGroupedPointEntry
import io.github.koalaplot.core.bar.DefaultVerticalBarPosition
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.generateHueColorPalette
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph

private const val HOUR_UNIT = "hr"
private const val MINUTE_UNIT = "min"
private const val SECOND_UNIT = "sec"

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    series: List<GraphSeries>,
    modifier: Modifier = Modifier,
    xAxisLabel: ReportXAxis?,
    yAxisLabel: String,
    isDurationType: Boolean,
    compactMode: Boolean = false,

) {
    val allXValues = remember(series) {
        series.flatMap { it.data.map { row -> row.xAxis } }.distinct().sorted()
    }

    val xValueToIndex = remember(allXValues) {
        allXValues.withIndex().associate { (index, xValue) -> xValue to index }
    }

    // Collect all unique subgroups, treating null as a separate group
    val allSubgroups = remember(series) {
        series.flatMap { s ->
            s.data.map { it.subgroup ?: "" }
        }.distinct()
    }

    // Generate color palette for all subgroups
    val subgroupColors = remember(allSubgroups) { generateHueColorPalette(allSubgroups.size) }
    val colorMap = remember(allSubgroups) {
        allSubgroups.associateWith { subgroup ->
            subgroupColors.getOrNull(allSubgroups.indexOf(subgroup)) ?: Color.Gray
        }
    }

    // Determine Y-axis unit and conversion factor
    val maxY = remember(series) {
        series.flatMap { it.data.map { row -> row.yAxis } }.maxOrNull() ?: 0.0
    }

    val (conversionFactor, unit) = remember(isDurationType, maxY) {
        calculateConversionFactor(isDurationType, maxY)
    }

    // Process bar series data
    val barSeries = series.filter { it.type == SeriesType.BAR }
    val barEntries = remember(barSeries, allXValues, conversionFactor, allSubgroups) {
        allXValues.map { xValue ->
            val subgroupValues = allSubgroups.map { subgroup ->
                calculateSubgroupValues(barSeries, xValue, subgroup, conversionFactor)
            }.map { DefaultVerticalBarPosition(0f, it) }

            DefaultVerticalBarPlotGroupedPointEntry(
                x = xValueToIndex[xValue]!!.toFloat(),
                y = subgroupValues
            )
        }
    }

    // Process line series data
    val lineSeries = series.filter { it.type == SeriesType.LINE }

    // Calculate Y-axis range
    val yRange = remember(series, conversionFactor) {
        val maxYValue = series.flatMap {
            it.data.map { (it.yAxis * conversionFactor).toFloat() }
        }.maxOrNull() ?: 0f

        // Handle case where all values are zero
        val adjustedMax = if (maxYValue == 0f) 1f else maxYValue * 1.1f
        0f..adjustedMax
    }

    // Determine step size for count-based Y-axis
    val tickIncrement = remember(yRange, yAxisLabel) {
        calculateTickIncrement(yRange, yAxisLabel, unit, isDurationType)
    }

    ChartLayout(
        modifier = modifier,
        legend = { if (!compactMode) CombinedLegend(series, colorMap) else null },
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYGraph(
            modifier = modifier
                .fillMaxSize(),
            xAxisModel = FloatLinearAxisModel(
                range = (-0.5f)..(allXValues.size - 0.5f),
                minimumMajorTickIncrement = 1f
            ),
            yAxisModel = FloatLinearAxisModel(
                range = yRange,
                minimumMajorTickIncrement = tickIncrement
            ),
            xAxisLabels = {
                val index = it.toInt()
                val rawValue = allXValues.getOrNull(index)
                val label = when (xAxisLabel) {
                    ReportXAxis.GENDER -> getGenderLabel(rawValue)
                    else -> rawValue?.toString() ?: ""
                }
                AxisValue(
                    label = label,
                    Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                )
            },
            xAxisTitle = { if (!compactMode) AxisLabels(xAxisLabel?.name ?: "") },
            yAxisLabels = {
                val formattedValue = if (isDurationType) {
                    "%.1f %s".format(it, unit)  // Shows "1.5 hr" format
                } else {
                    "%.0f".format(it)  // Shows whole numbers for counts
                }

                AxisValue(formattedValue, Modifier.defaultItemPadding(end = 4.dp))
            },
            yAxisTitle = {
                if (!compactMode)
                AxisLabels(
                    yAxisLabel,
                    Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                )
            }
        ) {
            // Draw bar series
            if (barSeries.isNotEmpty()) {
                GroupedVerticalBarPlot(
                    data = barEntries,
                    bar = { _, subgroupIndex, entry ->
                        val subgroup =
                            allSubgroups.getOrNull(subgroupIndex) ?: return@GroupedVerticalBarPlot
                        val color = colorMap[subgroup] ?: Color.Gray
                        DefaultVerticalBar(
                            brush = SolidColor(color),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HoverSurface {
                                Text(
                                    if (isDurationType) "%.1f %s".format(
                                        entry.y[subgroupIndex].yMax,
                                        unit
                                    )
                                    else "%.0f".format(entry.y[subgroupIndex].yMax)
                                )
                            }
                        }
                    },
                    maxBarGroupWidth = 1f / allSubgroups.size.coerceAtLeast(2)
                )
            }

            // Draw line series
            lineSeries.forEach { series ->
                series.data.groupBy { it.subgroup }.forEach { (subgroup, dataPoints) ->
                    val points = dataPoints.map {
                        DefaultPoint(
                            x = xValueToIndex[it.xAxis]!!.toFloat(),
                            y = (it.yAxis * conversionFactor).toFloat()
                        )
                    }
                    LinePlot(
                        data = points,
                        lineStyle = LineStyle(
                            brush = SolidColor(colorMap[subgroup] ?: Color.Black),
                            strokeWidth = 2.dp
                        ),
                        symbol = {
                            Symbol(
                                shape = RoundedCornerShape(4.dp),
                                fillBrush = SolidColor(colorMap[subgroup] ?: Color.Black),
                                modifier = Modifier.hoverableElement {
                                    HoverSurface { Text("%.1f %s".format(it.y, unit)) }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun getGenderLabel(rawValue: Any?): String {
    return when (rawValue as? String) {
        "0" -> stringResource(MR.strings.male)
        "1" -> stringResource(MR.strings.female)
        else -> rawValue?.toString() ?: ""
    }
}

@Composable
private fun CombinedLegend(
    series: List<GraphSeries>,
    colorMap: Map<String, Color>
) {
    // Filter series with non-empty subgroups
    val barSeriesMap = series.filter { it.type == SeriesType.BAR }
        .groupBy { it.name }
        .mapValues { entry ->
            entry.value.flatMap { it.data }
                .mapNotNull { it.subgroup }
                .filter { it.isNotEmpty() }
                .distinct()
        }
        .filterValues { it.isNotEmpty() }

    val lineSeriesMap = series.filter { it.type == SeriesType.LINE }
        .groupBy { it.name }
        .mapValues { entry ->
            entry.value.flatMap { it.data }
                .mapNotNull { it.subgroup }
                .filter { it.isNotEmpty() }
                .distinct()
        }
        .filterValues { it.isNotEmpty() }

    if (barSeriesMap.isEmpty() && lineSeriesMap.isEmpty()) return

    Surface(
        shadowElevation = 2.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = Color.Black,
            shape = RoundedCornerShape(5.dp)
        )
    ) {
        Column(
            modifier = Modifier.defaultItemPadding(
                start = 5.dp,
                end = 5.dp,
                top = 5.dp,
                bottom = 5.dp
            )
        ) {
            // Show bar series legends only if they have subgroups
            barSeriesMap.forEach { (seriesName, subgroups) ->
                if (subgroups.isNotEmpty()) {
                    Text(seriesName)
                    FlowLegend(
                        itemCount = subgroups.size,
                        symbol = { index ->
                            val subgroup = subgroups[index]
                            LegendItem(
                                label = subgroup,
                                color = colorMap[subgroup] ?: Color.Gray
                            )
                        }
                    )
                }
            }

            // Show line series legends only if they have subgroups
            lineSeriesMap.forEach { (seriesName, subgroups) ->
                if (subgroups.isNotEmpty()) {
                    Text(seriesName)
                    FlowLegend(
                        itemCount = subgroups.size,
                        symbol = { index ->
                            val subgroup = subgroups[index]
                            LegendItem(
                                label = subgroup,
                                color = colorMap[subgroup] ?: Color.Gray
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun LegendItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Symbol(
            modifier = Modifier.size(16.dp),
            fillBrush = SolidColor(color),
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}

@Composable
private fun AxisLabels(label: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        label,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}
@Composable
private fun AxisValue(label: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        label,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = androidx.compose.ui.text.style.TextAlign.End,
        fontSize = 8.sp
    )
}

@Composable
private fun HoverSurface(content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 4.dp,
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.defaultItemPadding(4.dp)
    ) {
        Box(modifier = Modifier.defaultItemPadding(8.dp)) {
            content()
        }
    }
}

private fun calculateSubgroupValues(
    barSeries: List<GraphSeries>,
    xValue: Any,
    subgroup: String,
    conversionFactor: Double
): Float {
    return barSeries.flatMap { barSeriesItem ->
        barSeriesItem.data.filter { dataPoint ->
            dataPoint.xAxis == xValue && (dataPoint.subgroup ?: "") == subgroup
        }
    }.firstOrNull()?.yAxis?.times(conversionFactor)?.toFloat() ?: 0f
}

private fun calculateConversionFactor(isDuration: Boolean, maxY: Double): Pair<Double, String> {
    return when {
        isDuration -> {
            when {
                maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, HOUR_UNIT)
                maxY >= 60_000 -> Pair(1.0 / 60_000, MINUTE_UNIT)
                else -> Pair(1.0 / 1_000, SECOND_UNIT)
            }
        }

        else -> Pair(1.0, "")
    }
}

private fun calculateTickIncrement(
    yRange: ClosedFloatingPointRange<Float>,
    yAxisLabel: String,
    unit: String,
    isDurationType: Boolean
): Float {
    val range = yRange.endInclusive - yRange.start
    return when {
        range == 0f -> 1f
        yAxisLabel.equals(YAxisTypes.COUNT.name, ignoreCase = true) -> {
            when {
                range < 10 -> 1f
                range < 100 -> 10f
                range < 1000 -> 50f
                else -> 100f
            }
        }

        else -> {
            when (unit) {
                HOUR_UNIT -> 0.5f
                MINUTE_UNIT -> 15f
                else -> 30f
            }
        }
    }.coerceAtMost(
        if (isDurationType) {
            range / 5
        } else {
            1f
        }
    )
}

fun Modifier.defaultChartPadding() = this
    .defaultScreenPadding()
    .defaultItemPadding(top = 4.dp, end = 4.dp)