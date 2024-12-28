package com.ustadmobile.core.domain.report.model

import kotlinx.serialization.Serializable

/**
 * Represents options selected by the user to generate a report. This is serialized into JSON
 * which is saved as to a string field on the Report entity.
 */
@Serializable
data class ReportOptions2(
    val title: String = "",
    val xAxis: String? = null,
    val series: List<ReportSeries2> = emptyList()
)
