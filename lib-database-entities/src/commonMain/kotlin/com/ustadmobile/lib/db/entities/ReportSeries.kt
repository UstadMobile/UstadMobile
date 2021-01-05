package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ReportSeries{

    var reportSeriesUid: Long = 0

    var reportSeriesName: String? = null

    var reportSeriesDataSet: Int = 0

    var reportSeriesVisualType: Int = 0

    var reportSeriesSubGroup: Int = NONE

    var reportSeriesFilters: List<ReportFilter>? = null

    companion object {

        const val TOTAL_DURATION = 200

        const val AVERAGE_DURATION = 201

        const val NUMBER_SESSIONS = 202

        const val ACTIVITIES_RECORDED = 203

        const val AVERAGE_SESSION_PER_CONTENT = 204

        const val PERCENT_STUDENTS_COMPLETED = 205

        const val NUMBER_STUDENTS_COMPLETED = 206

        const val NONE = 0

    }

}