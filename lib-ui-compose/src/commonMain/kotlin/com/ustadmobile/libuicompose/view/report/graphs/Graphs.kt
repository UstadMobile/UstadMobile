package com.ustadmobile.libuicompose.view.report.graphs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.utils.ReportFormatter
import com.ustadmobile.core.domain.report.utils.getDistinctSortedXValues
import com.ustadmobile.core.domain.report.utils.getDistinctSubgroups
import com.ustadmobile.core.domain.report.utils.getMaxYValue
import com.ustadmobile.core.domain.report.utils.toIndexMap
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.view.report.detail.getGenderLabel
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


/**
 * Bar chart positioning constants that ensure proper bar centering and spacing.
 *
 * The -0.5 offset is standard practice in data visualization to:
 * 1. Center bars perfectly on their tick marks
 * 2. Prevent edge clipping of first/last bars
 * 3. Match industry standards (Matplotlib/ggplot2/D3.js use similar approaches)
 *
 * Visual explanation:
 * Without: |█0█1█2| (clipped edges)
 * With:    █0 █1 █2  (proper spacing)
 */
private const val BAR_POSITION_OFFSET = 0.5f

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun CombinedGraph(
    series: List<GraphSeries>,
    modifier: Modifier = Modifier,
    yAxisLabel: String,
    isDurationType: Boolean,
    compactMode: Boolean = false,
    reportOptions: ReportOptions2? = null,
) {
    val hourUnit = stringResource(MR.strings.hour_unit)
    val minuteUnit = stringResource(MR.strings.minute_unit)
    val secondUnit = stringResource(MR.strings.second_unit)

    val xAxisLabel = reportOptions?.xAxis ?: ReportXAxis.DAY

    val allXValues = remember(series) {
        series.getDistinctSortedXValues()
    }

    val xValueToIndex = remember(allXValues) {
        allXValues.toIndexMap()
    }

    val allSubgroups = remember(series) {
        series.getDistinctSubgroups()
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
        series.getMaxYValue()
    }

    val (conversionFactor, unit) = remember(isDurationType, maxY) {
        calculateConversionFactor(
            isDurationType, maxY,
            hourUnit = hourUnit,
            minuteUnit = minuteUnit,
            secondUnit = secondUnit
        )
    }

    // Process bar series data
    val barSeries = series.filter { it.type == SeriesType.BAR }
    val barEntries = remember(barSeries, allXValues, conversionFactor, allSubgroups) {
        allXValues.mapIndexed { index, xValue ->
            val subgroupValues = allSubgroups.map { subgroup ->
                calculateSubgroupValues(barSeries, xValue, subgroup, conversionFactor)
            }.map { DefaultVerticalBarPosition(0f, it) }

            DefaultVerticalBarPlotGroupedPointEntry(
                x = index.toFloat(),
                y = subgroupValues
            )
        }
    }

    // Process line series data
    val lineSeries = series.filter { it.type == SeriesType.LINE }

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
        calculateTickIncrement(
            yRange, yAxisLabel, unit, isDurationType, hourUnit = hourUnit,
            minuteUnit = minuteUnit,
        )
    }

    ChartLayout(
        modifier = modifier,
        legend = { if (!compactMode) CombinedLegend(series, colorMap, reportOptions) },
        legendLocation = LegendLocation.BOTTOM
    ) {
        XYGraph(
            modifier = modifier
                .fillMaxSize(),
            xAxisModel = FloatLinearAxisModel(
                range = (-BAR_POSITION_OFFSET)..(allXValues.size - BAR_POSITION_OFFSET),
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
                    ReportXAxis.GENDER -> getGenderLabel(rawValue.toString())
                    ReportXAxis.CLASS -> rawValue
                        ?: ""

                    else -> rawValue?.let { value ->
                        ReportFormatter.formatDateForReport(
                            value.toString(),
                            xAxisLabel
                        )
                    } ?: ""
                }
                AxisValue(
                    label = label.toString(),
                    Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                )
            },
            xAxisTitle = { if (!compactMode) AxisLabels(stringResource( xAxisLabel.label)) },
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
                        val subgroup = allSubgroups.getOrNull(subgroupIndex) ?: return@GroupedVerticalBarPlot
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
                    val points = dataPoints.mapNotNull { dataPoint ->
                        xValueToIndex[dataPoint.xAxis]?.let { index ->
                            DefaultPoint(
                                x = index.toFloat(),
                                y = (dataPoint.yAxis * conversionFactor).toFloat()
                            )
                        }
                    }

                    if (points.isNotEmpty()) {
                        val lineColor = colorMap[subgroup] ?: Color.Black
                        LinePlot(
                            data = points,
                            lineStyle = LineStyle(
                                brush = SolidColor(lineColor),
                                strokeWidth = 2.dp
                            ),
                            symbol = {
                                Symbol(
                                    shape = RoundedCornerShape(4.dp),
                                    fillBrush = SolidColor(lineColor),
                                    modifier = Modifier.hoverableElement {
                                        HoverSurface {
                                            Text(
                                                if (isDurationType) "%.1f %s".format(it.y, unit)
                                                else "%.0f".format(it.y)
                                            )
                                        }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun CombinedLegend(
    series: List<GraphSeries>,
    colorMap: Map<String, Color>,
    reportOptions: ReportOptions2? = null,
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
            shape = RoundedCornerShape(6.dp)
        )
    ) {
        Column(
            modifier = Modifier.defaultItemPadding(
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
                                label = formatSubgroupValue(subgroup, reportOptions),
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
                                label = formatSubgroupValue(subgroup, reportOptions),
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
    Text(
        label,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun AxisValue(label: String, modifier: Modifier = Modifier) {
    Text(
        label,
        modifier = modifier.fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        textAlign = TextAlign.End,
        fontSize = 8.sp
    )
}

@Composable
private fun HoverSurface(content: @Composable () -> Unit) {
    Surface(
        shadowElevation = 4.dp,
        shape =shapes.medium,
        color = colorScheme.surfaceContainerHighest,
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

private fun calculateConversionFactor(
    isDuration: Boolean,
    maxY: Double,
    hourUnit: String,
    minuteUnit: String,
    secondUnit: String
): Pair<Double, String> {
    return when {
        isDuration -> {
            when {
                maxY >= 3_600_000 -> Pair(1.0 / 3_600_000, hourUnit)
                maxY >= 60_000 -> Pair(1.0 / 60_000, minuteUnit)
                else -> Pair(1.0 / 1_000, secondUnit)
            }
        }

        else -> Pair(1.0, "")
    }
}

@Composable
fun formatSubgroupValue(
    value: String,
    reportOptions: ReportOptions2?,
): String {
    return reportOptions?.series?.firstOrNull()?.reportSeriesSubGroup?.let { axisType ->
        when (axisType) {
            ReportXAxis.GENDER -> getGenderLabel(value)
            ReportXAxis.CLASS -> value
            else -> ReportFormatter.formatDateForReport(value, axisType)
        }
    } ?: value
}

private fun calculateTickIncrement(
    yRange: ClosedFloatingPointRange<Float>,
    yAxisLabel: String,
    unit: String,
    isDurationType: Boolean,
    hourUnit: String,
    minuteUnit: String,
): Float {
    val range = yRange.endInclusive - yRange.start
    return when {
        range == 0f -> 1f
        yAxisLabel.equals(YAxisTypes.COUNT.label) -> {
            when {
                range < 10 -> 1f
                range < 100 -> 10f
                range < 1000 -> 50f
                else -> 100f
            }
        }

        else -> {
            when (unit) {
                hourUnit -> 0.5f
                minuteUnit -> 15f
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
