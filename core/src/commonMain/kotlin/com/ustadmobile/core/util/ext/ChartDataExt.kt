package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.graph.CodeLabelFormatter
import com.ustadmobile.lib.db.entities.Report

fun ChartData.setCountryMap(countryMap: Map<String, String>){
    seriesData.forEach { data ->
        if(data.series.reportSeriesSubGroup == Report.COUNTRY) {
            (data.subGroupFormatter as CodeLabelFormatter).map = countryMap
        }
    }
    if(xAxisValueFormatter is CodeLabelFormatter){
        xAxisValueFormatter.map = countryMap
    }
}