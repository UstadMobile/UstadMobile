package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
data class StatementReportData(var yAxis: Float = 0f, var xAxis: String? = "", var subgroup: String? = "")