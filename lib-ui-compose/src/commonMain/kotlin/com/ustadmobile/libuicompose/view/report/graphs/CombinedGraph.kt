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
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.YAxisTypes
import com.ustadmobile.core.domain.report.utils.DefaultXAxisLabelFormatter
import com.ustadmobile.core.domain.report.utils.ReportXAxisLabelFormatter
import com.ustadmobile.core.domain.report.utils.getDistinctSortedXValues
import com.ustadmobile.core.domain.report.utils.getDistinctSubgroups
import com.ustadmobile.core.domain.report.utils.getMaxYValue
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel


//Create a Preview for this - see what it looks like.
@Composable
fun CombinedGraph(
    graphSeries: List<GraphSeries>,
    reportOptions: ReportOptions2 = ReportOptions2(),
    modifier: Modifier = Modifier,
    xAxisLabelFormatter: ReportXAxisLabelFormatter = DefaultXAxisLabelFormatter()
) {
    // Get all distinct x-axis values (categories)
    // THESE ARE NOT CATEGORIES - BAD NAMING
    val categories = remember(graphSeries) {
        graphSeries.getDistinctSortedXValues()
    }

    // Format the categories for display
    val formattedCategories = remember(categories, reportOptions.xAxis, xAxisLabelFormatter) {
        categories.map { xAxisLabelFormatter.formatLabel(it, reportOptions.xAxis) }
    }

    // Get all distinct subgroups (series)
    val subgroups = remember(graphSeries) {
        graphSeries.getDistinctSubgroups()
    }

    // Generate colors for each subgroup
    val colors = remember(subgroups) {
        List(subgroups.size) { index ->
            Color.hsv(index * 360f / subgroups.size.coerceAtLeast(1), 1f, 0.7f)
        }
    }

    // Calculate max value for Y-axis scaling with fallback
    val yRange = remember(graphSeries) {
        val maxValue = graphSeries.getMaxYValue()
        if (maxValue > 0) 0f..(maxValue * 1.1).toFloat() else 0f..1f
    }

    val xAxisLabel = reportOptions.xAxis
    val isDurationType = reportOptions.series.any { it.reportSeriesYAxis.type == YAxisTypes.DURATION }
    val yAxisLabel = stringResource(if (isDurationType) YAxisTypes.DURATION.label else YAxisTypes.COUNT.label)

    //As per https://koalaplot.github.io/0.5/docs/xygraphs/bar_plots/#grouped-bars
    XYGraph(
        modifier = modifier,
        xAxisModel = CategoryAxisModel(formattedCategories),
        yAxisModel = rememberFloatLinearAxisModel(yRange),
        xAxisLabels = {
            AxisValue(
                label = it.toString(),
                modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
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
            AxisValue(it.toString(), Modifier.defaultItemPadding(end = 4.dp))
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
            //The XAxis is ALREADY SORT#ED - there is no need to do that again. There is no need to deduplicate it.
            graphSeries.getDistinctSortedXValues().forEachIndexed { xAxisIndex, xAxis ->
                series(solidBar(colors[xAxisIndex])) {
                    graphSeries.forEachIndexed { seriesIndex, series ->
                        //Not all reportSeries will have a bar for all xAxis values
                        series.data.firstOrNull { it.xAxis == xAxis }?.also { dataPoint ->
                            item(xAxis, 0f, dataPoint.yAxis.toFloat())
                        }
                    }
                }
            }
        }
    }
}