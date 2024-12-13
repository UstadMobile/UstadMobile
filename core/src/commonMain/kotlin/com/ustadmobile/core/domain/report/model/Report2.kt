package com.ustadmobile.core.domain.report.model

import kotlinx.serialization.Serializable

@Serializable
data class Report2(
    val title: String,
    val series: List<ReportSeries2>
)
