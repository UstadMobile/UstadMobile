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
        yAxisModel = rememberFloatLinearAxisModel(reportResult.yRange),
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
                text = it.toString(),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        yAxisTitle = {
            Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    text = if(reportResult.yAxisType == YAxisTypes.DURATION) {
                        stringResource(MR.strings.duration) //TODO here: add unit
                    }else {
                        stringResource(MR.strings.count)
                    }
                )
            }
        }
    ) {
        GroupedVerticalBarPlot {
            reportResult.distinctSubgroups.forEachIndexed { subgroupIndex, subgroup ->
                series(solidBar(colors[subgroupIndex])) {
                    // For each x value, find matching data points
                    reportResult.distinctXAxisValueSorted.forEach { xValue ->
                        reportResult.resultSeries.firstNotNullOfOrNull { seriesItem ->
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
    }
}
