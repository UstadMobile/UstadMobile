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

}