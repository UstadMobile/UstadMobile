package com.ustadmobile.core.domain.report.model

import com.ustadmobile.lib.db.entities.DateRangeMoment
import kotlinx.serialization.Serializable

@Serializable
data class ReportSeries2(

    var reportSeriesUid: Int = 0,

    var reportSeriesTitle: String = "",

    var reportSeriesYAxis: ReportSeriesYAxis? = ReportSeriesYAxis.TOTAL_DURATION,

    var reportSeriesVisualType: ReportSeriesVisualType? = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.DAY,

    var reportSeriesFilters: List<ReportFilter2>? = null,

    var reportTimeRange: ReportTimeRange? = ReportTimeRange.LAST_WEEK

)

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis {
    TOTAL_DURATION,
    AVERAGE_DURATION,
    NUMBER_SESSIONS,
    INTERACTIONS_RECORDED,
    NUMBER_ACTIVE_USERS,
    AVERAGE_USAGE_TIME_PER_USER,
    NUMBER_OF_STUDENTS_COMPLETED_CONTENT,
    PERCENT_OF_STUDENTS_COMPLETED_CONTENT,
    TOTAL_ATTENDANCE,
    TOTAL_ABSENCES,
    TOTAL_LATES,
    PERCENTAGE_STUDENTS_ATTENDED,
    PERCENTAGE_STUDENTS_ATTENDED_OR_LATE,
    TOTAL_CLASSES,
    NUMBER_UNIQUE_STUDENTS_ATTENDING,
    NONE,
    ATTENDANCE_QUERY,
    STATEMENT_QUERY
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType {
    BAR_CHART,
    LINE_GRAPH
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis {
    DAY,
    WEEK,
    MONTH,
    CLASS,
    GENDER
}

/** Enum representing different time range options for report series */
enum class ReportTimeRange {
    LAST_WEEK,
    LAST_MONTH
}

/** Enum representing different filter types for report series */
enum class FilterType {
    PERSON_AGE,
    PERSON_GENDER
}
