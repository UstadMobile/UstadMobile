package com.ustadmobile.core.domain.report.model

import com.ustadmobile.lib.db.entities.DateRangeMoment
import kotlinx.serialization.Serializable

@Serializable
data class ReportSeries2(

    var reportSeriesUid: Int = 0,

    var reportSeriesTitle: String = "",

    var reportSeriesYAxis: ReportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,

    var reportSeriesVisualType: ReportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis = ReportXAxis.DAY,

    var reportSeriesFilters: List<ReportFilter2>? = null,

    var reportTimeRange: ReportTimeRange = ReportTimeRange.LAST_WEEK

)

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis(val id: Int) {
    TOTAL_DURATION(1),
    AVERAGE_DURATION(2),
    NUMBER_SESSIONS(3),
    INTERACTIONS_RECORDED(4),
    NUMBER_ACTIVE_USERS(5),
    AVERAGE_USAGE_TIME_PER_USER(6),
    NUMBER_OF_STUDENTS_COMPLETED_CONTENT(7),
    PERCENT_OF_STUDENTS_COMPLETED_CONTENT(8),
    TOTAL_ATTENDANCE(9),
    TOTAL_ABSENCES(10),
    TOTAL_LATES(11),
    PERCENTAGE_STUDENTS_ATTENDED(12),
    PERCENTAGE_STUDENTS_ATTENDED_OR_LATE(13),
    TOTAL_CLASSES(14),
    NUMBER_UNIQUE_STUDENTS_ATTENDING(15),
    NONE(16),
    ATTENDANCE_QUERY(17),
    STATEMENT_QUERY(18)
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(val id: Int) {
    BAR_CHART(1),
    LINE_GRAPH(2)
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(val id: Int) {
    DAY(1),
    WEEK(2),
    MONTH(3),
    CLASS(4),
    GENDER(5)
}

enum class ReportTimeRange(val id: Int) {
    LAST_WEEK(1),
    LAST_MONTH(2),
    LAST_YEAR(3),
}
