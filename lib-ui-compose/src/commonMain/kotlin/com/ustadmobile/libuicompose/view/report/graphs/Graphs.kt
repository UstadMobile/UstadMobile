package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
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



@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    series: List<GraphSeries>,
    modifier: Modifier = Modifier,
    xAxisLabel: String,
    yAxisLabel: String
) {
    // Collect all unique xAxis values from all series
    val allXValues = remember(series) {
        series.flatMap { it.data.map { row -> row.xAxis } }.distinct().sorted()
    }

    // Create a map from xAxis value to its index
    val xValueToIndex = remember(allXValues) {
        allXValues.withIndex().associate { (index, xValue) -> xValue to index }
    }

    // Collect all unique subgroups, treating null as a separate group
    val allSubgroups = remember(series) {
        series.flatMap { s ->
            s.data.map { it.subgroup ?: "Default" }
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
    val (conversionFactor, unit) = remember(yAxisLabel) {
        if (yAxisLabel.equals(YAxisTypes.DURATION.name, ignoreCase = true)) {
            val maxY = series.flatMap { it.data.map { row -> row.yAxis } }.maxOrNull() ?: 0.0
            when {
                maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, "hr")
                maxY >= 60_000 -> Pair(1.0 / 60_000, "min")
                else -> Pair(1.0 / 1_000, "sec")
            }
        } else {
            Pair(1.0, yAxisLabel)
        }
    }

    // Process bar series data
    val barSeries = series.filter { it.type == SeriesType.BAR }
    val barEntries = remember(barSeries, allXValues, conversionFactor, allSubgroups) {
        allXValues.map { xValue ->
            val subgroupValues = allSubgroups.map { subgroup ->
                barSeries.flatMap { bs ->
                    bs.data.filter { it.xAxis == xValue && (it.subgroup ?: "Default") == subgroup }
                }.firstOrNull()?.yAxis ?: 0.0
            }.map { (it * conversionFactor).toFloat() }

            DefaultVerticalBarPlotGroupedPointEntry(
                x = xValueToIndex[xValue]!!.toFloat(),
                y = subgroupValues.map { DefaultVerticalBarPosition(0f, it) }
            )
        }
    }

    // Process line series data
    val lineSeries = series.filter { it.type == SeriesType.LINE }

    // Calculate Y-axis range
    val yRange = remember(series, conversionFactor) {
        val maxY =
            series.flatMap { it.data.map { (it.yAxis * conversionFactor).toFloat() } }.maxOrNull()
                ?: 0f
        0f..(maxY * 1.1f)
    }

    // Determine step size for count-based Y-axis
    val tickIncrement = remember(yRange, yAxisLabel) {
        val range = yRange.endInclusive - yRange.start
        val defaultIncrement = when {
            yAxisLabel.equals(YAxisTypes.COUNT.name, ignoreCase = true) -> {
                when {
                    range < 10 -> 1f
                    range < 100 -> 10f
                    range < 1000 -> 50f
                    else -> 100f
                }
            }

            else -> { // For duration
                when (unit) {
                    "hr" -> 0.5f
                    "min" -> 15f
                    else -> 30f
                }
            }
        }
        defaultIncrement.coerceAtMost(range / 5) // Prevents `tickIncrement` from exceeding range
    }

    ChartLayout(
        modifier = modifier.defaultScreenPadding(),
        legend = { CombinedLegend(series, colorMap) },
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYGraph(
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
                AxisLabels(
                    allXValues.getOrNull(index) ?: "",
                    Modifier.defaultItemPadding(top = 4.dp)
                )
            },
            xAxisTitle = { AxisLabels(xAxisLabel) },
            yAxisLabels = {
                AxisLabels(
                    "%.1f".format(it),
                    Modifier
                        .defaultItemPadding(end = 4.dp)
                )
            },
            yAxisTitle = {
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
                                Text("%.1f %s".format(entry.y[subgroupIndex].yMax, unit))
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

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun CombinedLegend(
    series: List<GraphSeries>,
    colorMap: Map<String, Color>
) {
    val barSeriesMap = series.filter { it.type == SeriesType.BAR }
        .groupBy { it.name }
        .mapValues { entry -> entry.value.flatMap { it.data }.map { it.subgroup ?: "" }.distinct() }

    val lineSeriesMap = series.filter { it.type == SeriesType.LINE }
        .groupBy { it.name }
        .mapValues { entry -> entry.value.flatMap { it.data }.map { it.subgroup ?: "" }.distinct() }

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
            barSeriesMap.forEach { (seriesName, subgroups) ->
                if (subgroups.isNotEmpty()) {
                    Text(seriesName)
                    FlowLegend(
                        itemCount = subgroups.size,
                        symbol = { index ->
                            val subgroup = subgroups[index]
                            Symbol(
                                modifier = Modifier.size(16.dp),
                                fillBrush = colorMap[subgroup]?.let { SolidColor(it) },
                                shape = RoundedCornerShape(4.dp)
                            )
                        },
                        label = { index ->
                            subgroups[index].let { Text(it) }
                        },
                    )
                }
            }

            lineSeriesMap.forEach { (seriesName, subgroups) ->
                if (subgroups.isNotEmpty()) {
                    Text(seriesName)
                    FlowLegend(
                        itemCount = subgroups.size,
                        symbol = { index ->
                            val subgroup = subgroups[index]
                            Symbol(
                                modifier = Modifier.size(16.dp),
                                fillBrush = colorMap[subgroup]?.let { SolidColor(it) },
                                shape = RoundedCornerShape(4.dp)
                            )
                        },
                        label = { index ->
                            subgroups[index]?.let { Text(it) }
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun AxisLabels(label: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(
        label,
        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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