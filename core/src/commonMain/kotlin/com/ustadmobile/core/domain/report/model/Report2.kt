package com.ustadmobile.core.domain.report.model

import kotlinx.serialization.Serializable

@Serializable
data class Report2(
    val title: String = "",
    val xAxis: ReportXAxis = ReportXAxis.DAY,
    val series: List<ReportSeries2> = emptyList()
)
