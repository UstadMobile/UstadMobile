package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportSeries{

    var reportSeriesUid: Int = 0

    var reportSeriesName: String? = null

    var reportSeriesYAxis: Int = TOTAL_DURATION

    var reportSeriesVisualType: Int = BAR_CHART

    var reportSeriesSubGroup: Int = NONE

    var reportSeriesFilters: List<ReportFilter>? = null



    companion object {

        const val BAR_CHART = 100

        const val LINE_GRAPH = 101

        const val STATEMENT_QUERY = 100

        const val ATTENDANCE_QUERY = 101

        const val TOTAL_DURATION = 200

        const val AVERAGE_DURATION = 201

        const val NUMBER_SESSIONS = 202

        const val INTERACTIONS_RECORDED = 203

        const val NUMBER_ACTIVE_USERS = 204

        const val AVERAGE_USAGE_TIME_PER_USER = 205

        const val NUMBER_OF_STUDENTS_COMPLETED_CONTENT = 206

        const val PERCENT_OF_STUDENTS_COMPLETED_CONTENT = 207

        const val TOTAL_ATTENDANCE = 208

        const val TOTAL_ABSENCES = 209

        const val TOTAL_LATES = 210

        const val PERCENTAGE_STUDENTS_ATTENDED = 211

        const val PERCENTAGE_STUDENTS_ATTENDED_OR_LATE = 212

        const val TOTAL_CLASSES = 213

        const val NUMBER_UNIQUE_STUDENTS_ATTENDING = 214

        const val NONE = 0

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ReportSeries

        if (reportSeriesUid != other.reportSeriesUid) return false
        if (reportSeriesName != other.reportSeriesName) return false
        if (reportSeriesYAxis != other.reportSeriesYAxis) return false
        if (reportSeriesVisualType != other.reportSeriesVisualType) return false
        if (reportSeriesSubGroup != other.reportSeriesSubGroup) return false
        if (reportSeriesFilters != other.reportSeriesFilters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportSeriesUid
        result = 31 * result + (reportSeriesName?.hashCode() ?: 0)
        result = 31 * result + reportSeriesYAxis
        result = 31 * result + reportSeriesVisualType
        result = 31 * result + reportSeriesSubGroup
        result = 31 * result + (reportSeriesFilters?.hashCode() ?: 0)
        return result
    }

}