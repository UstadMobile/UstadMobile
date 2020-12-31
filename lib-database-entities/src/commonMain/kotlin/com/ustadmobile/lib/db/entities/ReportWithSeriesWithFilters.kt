package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportWithSeriesWithFilters() : Report() {

    constructor(report: Report, reportSeries: List<ReportSeries> = listOf()) : this() {
        this.reportUid = report.reportUid
        this.reportTitle = report.reportTitle
        this.reportOwnerUid = report.reportOwnerUid
        this.reportInactive = report.reportInactive
        this.fromDate = report.fromDate
        this.toDate = report.toDate
        this.xAxis = report.xAxis
        this.reportSeries = report.reportSeries
        reportSeriesWithFiltersList = reportSeries
    }

    var reportSeriesWithFiltersList = listOf<ReportSeries>()


    data class QueryParts(val sqlStr: String, val sqlListStr: String, val queryParams: Array<Any>)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ReportWithSeriesWithFilters

        if (reportSeriesWithFiltersList != other.reportSeriesWithFiltersList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + reportSeriesWithFiltersList.hashCode()
        return result
    }


    companion object{



    }
}