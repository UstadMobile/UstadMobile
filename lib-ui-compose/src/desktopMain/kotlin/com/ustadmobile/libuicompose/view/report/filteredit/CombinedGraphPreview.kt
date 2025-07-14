package com.ustadmobile.libuicompose.view.report.filteredit

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.query.RunReportUseCase
import com.ustadmobile.lib.db.composites.StatementReportRow
import com.ustadmobile.libuicompose.view.report.graphs.CombinedGraph

@Composable
@Preview
fun CombinedGraphPreview() {
    val series1 = RunReportUseCase.RunReportResult.Series(
        reportSeriesOptions = ReportSeries2(
            reportSeriesUid = 1,
            reportSeriesTitle = "Series 1",
            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
            reportSeriesSubGroup = ReportXAxis.GENDER
        ),
        data = listOf(
            StatementReportRow(yAxis = 10.0, xAxis = "10-12-2012", subgroup = "1"),
            StatementReportRow(yAxis = 15.0, xAxis = "11-12-2012", subgroup = "2"),
            StatementReportRow(yAxis = 8.0, xAxis = "12-12-2012", subgroup = "1")
        )
    )

    val series2 = RunReportUseCase.RunReportResult.Series(
        reportSeriesOptions = ReportSeries2(
            reportSeriesUid = 2,
            reportSeriesTitle = "Series 2",
            reportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,
            reportSeriesSubGroup = ReportXAxis.GENDER
        ),
        data = listOf(
            StatementReportRow(yAxis = 5.0, xAxis = "10-12-2012", subgroup = "2"),
            StatementReportRow(yAxis = 12.0, xAxis = "11-12-2012", subgroup = "2"),
            StatementReportRow(yAxis = 14.0, xAxis = "12-12-2012", subgroup = "1")
        )
    )

    val sampleSeries = listOf(series1, series2)

    Surface(modifier = Modifier.size(400.dp, 300.dp)) {
//        CombinedGraph(series = sampleSeries)
    }
}