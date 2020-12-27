package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters

fun ReportWithSeriesWithFilters.generateSql(): Map<ReportSeries,QueryParts>{

    val queryPartsList = mutableMapOf<ReportSeries, QueryParts>()
    reportSeriesWithFiltersList.forEach {
        queryPartsList[it] = it.toSql(this)
    }
    return queryPartsList.toMap()
}