package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph

@Composable
@Preview
fun CombinedGraphPreview() {
    // Sample data for preview using RunReportUseCase.Series
    val series1 = RunReportUseCase.RunReportResult.Series(
        reportSeriesOptions = ReportSeries2(
            reportSeriesUid = 1,
            reportSeriesTitle = "Series 1",
            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART
        ),
        data = listOf(
            StatementReportRow(yAxis = 10.0, xAxis = "Jan", subgroup = "Series 1"),
            StatementReportRow(yAxis = 15.0, xAxis = "Feb", subgroup = "Series 1"),
            StatementReportRow(yAxis = 8.0, xAxis = "Mar", subgroup = "Series 1")
        )
    )

    val series2 = RunReportUseCase.RunReportResult.Series(
        reportSeriesOptions = ReportSeries2(
            reportSeriesUid = 2,
            reportSeriesTitle = "Series 2",
            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART
        ),
        data = listOf(
            StatementReportRow(yAxis = 5.0, xAxis = "Jan", subgroup = "Series 2"),
            StatementReportRow(yAxis = 12.0, xAxis = "Feb", subgroup = "Series 2"),
            StatementReportRow(yAxis = 14.0, xAxis = "Mar", subgroup = "Series 2")
        )
    )

    val series3 = RunReportUseCase.RunReportResult.Series(
        reportSeriesOptions = ReportSeries2(
            reportSeriesUid = 3,
            reportSeriesTitle = "Series 3",
            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART
        ),
        data = listOf(
            StatementReportRow(yAxis = 7.0, xAxis = "Jan", subgroup = "Series 3"),
            StatementReportRow(yAxis = 9.0, xAxis = "Feb", subgroup = "Series 3"),
            StatementReportRow(yAxis = 11.0, xAxis = "Mar", subgroup = "Series 3")
        )
    )

    val sampleSeries = listOf(series1, series2, series3)

    Surface(modifier = Modifier.size(400.dp, 300.dp)) {
        CombinedGraph(
            series = sampleSeries,
        )
    }
}