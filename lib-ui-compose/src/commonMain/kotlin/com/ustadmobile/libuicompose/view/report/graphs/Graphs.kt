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
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.ext.defaultScreenPadding
import io.github.aakira.napier.Napier
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class SeriesType { BAR, LINE }

data class ReportResultQueryRow(
    val yAxis: Double,
    val xAxis: String,
    val subgroup: String?
)

data class GraphSeries(
    val type: SeriesType,
    val data: List<ReportResultQueryRow>,
    val name: String
)

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    series: List<GraphSeries>,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    // Collect all unique subgroups from all series
    val allSubgroups = remember(series) {
        series.flatMap { s ->
            s.data.mapNotNull { it.subgroup }
        }.distinct()
    }

    // Generate color palette for all subgroups
    val subgroupColors = remember(allSubgroups) { generateHueColorPalette(allSubgroups.size) }
    val colorMap = remember(allSubgroups) {
        allSubgroups.associateWith { subgroup ->
            subgroupColors.getOrNull(allSubgroups.indexOf(subgroup)) ?: Color.Gray
        }
    }

    // Get all dates from all series
    val allDates = remember(series) {
        series.flatMap { it.data.map { row -> LocalDate.parse(row.xAxis, dateFormatter) } }
            .toSortedSet()
            .toList()
    }

    // Determine Y-axis unit and conversion factor
    val (conversionFactor, unit) = remember(series) {
        val maxY = series.flatMap { it.data.map { row -> row.yAxis } }.maxOrNull() ?: 0.0
        when {
            maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, "hr")
            maxY >= 60_000 -> Pair(1.0 / 60_000, "min")
            else -> Pair(1.0 / 1_000, "sec")
        }
    }

    // Process bar series data
    val barSeries = series.filter { it.type == SeriesType.BAR }
    val barDataGrouped = remember(barSeries, allDates) {
        barSeries.flatMap { bs ->
            bs.data.groupBy { LocalDate.parse(it.xAxis, dateFormatter) }
                .map { (date, rows) -> date to rows.groupBy { it.subgroup } }
        }.toMap()
    }

    // Prepare bar entries for each date considering all subgroups
    val barEntries = remember(barSeries, allDates, conversionFactor, allSubgroups) {
        allDates.map { date ->
            val subgroupValues = allSubgroups.map { subgroup ->
                barSeries.flatMap { bs ->
                    barDataGrouped[date]?.get(subgroup) ?: emptyList()
                }.firstOrNull()?.yAxis ?: 0.0
            }.map { (it * conversionFactor).toFloat() }

            DefaultVerticalBarPlotGroupedPointEntry(
                x = date.toEpochDay().toFloat(),
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

    ChartLayout(
        modifier = modifier.defaultScreenPadding(),
        legend = { CombinedLegend(series, colorMap) },
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYGraph(
            xAxisModel = FloatLinearAxisModel(
                range = (allDates.first().toEpochDay().toFloat() - 0.5f)..
                        (allDates.last().toEpochDay().toFloat() + 0.5f),
                minimumMajorTickIncrement = 1f
            ),
            yAxisModel = FloatLinearAxisModel(
                range = yRange,
                minimumMajorTickIncrement = when (unit) {
                    "hr" -> 0.5f
                    "min" -> 15f
                    else -> 30f
                }
            ),
            xAxisLabels = {
                AxisLabels(
                    formatDates(LocalDate.ofEpochDay(it.toLong())),
                    Modifier.defaultItemPadding(top = 4.dp)
                )
            },
            xAxisTitle = { AxisLabels("Date") },
            yAxisLabels = {
                AxisLabels(
                    "%.1f".format(it),
                    Modifier
                        .defaultItemPadding(end = 4.dp)
                )
            },
            yAxisTitle = {
                AxisLabels(
                    "Content usage (hours)",
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
                    maxBarGroupWidth = 1f / allSubgroups.size.coerceAtLeast(1)
                )
            }

            // Draw line series
            lineSeries.forEach { series ->
                series.data.groupBy { it.subgroup }.forEach { (subgroup, dataPoints) ->
                    val points = dataPoints.map {
                        DefaultPoint(
                            x = LocalDate.parse(it.xAxis, dateFormatter).toEpochDay().toFloat(),
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
        .mapValues { entry -> entry.value.flatMap { it.data }.map { it.subgroup }.distinct() }

    val lineSeriesMap = series.filter { it.type == SeriesType.LINE }
        .groupBy { it.name }
        .mapValues { entry -> entry.value.flatMap { it.data }.map { it.subgroup }.distinct() }

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
                                fillBrush = SolidColor(colorMap[subgroup] ?: Color.Gray),
                                shape = RoundedCornerShape(4.dp)
                            )
                        },
                        label = { index ->
                            subgroups[index]?.let { Text(it) }
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
                                fillBrush = SolidColor(colorMap[subgroup] ?: Color.Gray),
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

private fun formatDates(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))