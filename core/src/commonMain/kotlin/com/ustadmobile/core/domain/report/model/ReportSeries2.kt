package com.ustadmobile.core.domain.report.model

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable

@Serializable
data class ReportSeries2(

    var reportSeriesUid: Int = 0,

    var reportSeriesTitle: String = "",

    var reportSeriesYAxis: ReportSeriesYAxis? = ReportSeriesYAxis.TOTAL_DURATION,

    var reportSeriesVisualType: ReportSeriesVisualType? = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.DAY,

    var reportSeriesFilters: List<ReportFilter3>? = null,

    var reportTimeRange: ReportTimeRange? = ReportTimeRange.LAST_WEEK

)

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis(val value: Int) {
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
    STATEMENT_QUERY(18);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            TOTAL_DURATION to MR.strings.total_duration,
            AVERAGE_DURATION to MR.strings.average_duration,
            NUMBER_SESSIONS to MR.strings.number_sessions,
            INTERACTIONS_RECORDED to MR.strings.interactions_recorded,
            NUMBER_ACTIVE_USERS to MR.strings.number_active_users,
            AVERAGE_USAGE_TIME_PER_USER to MR.strings.average_usage_time_per_user,
            NUMBER_OF_STUDENTS_COMPLETED_CONTENT to MR.strings.number_of_students_completed_content,
            PERCENT_OF_STUDENTS_COMPLETED_CONTENT to MR.strings.percent_of_students_completed_content,
            TOTAL_ATTENDANCE to MR.strings.total_attendance,
            TOTAL_ABSENCES to MR.strings.total_absences,
            TOTAL_LATES to MR.strings.total_lates,
            PERCENTAGE_STUDENTS_ATTENDED to MR.strings.percentage_students_attended,
            PERCENTAGE_STUDENTS_ATTENDED_OR_LATE to MR.strings.percentage_students_attended_or_late,
            TOTAL_CLASSES to MR.strings.total_classes,
            NUMBER_UNIQUE_STUDENTS_ATTENDING to MR.strings.number_unique_students_attending,
            NONE to MR.strings.none,
            ATTENDANCE_QUERY to MR.strings.attendance_query,
            STATEMENT_QUERY to MR.strings.statement_query
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForYAxis(yAxis: ReportSeriesYAxis): StringResource {
            return stringResourceMap[yAxis] ?: MR.strings.none // Fallback to `none` if not found
        }
    }
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(val value: Int) {
    BAR_CHART(1),
    LINE_GRAPH(2);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            BAR_CHART to MR.strings.bar_chart,
            LINE_GRAPH to MR.strings.line_chart
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForVisualType(visualType: ReportSeriesVisualType): StringResource {
            return stringResourceMap[visualType]
                ?: MR.strings.bar_chart // Fallback to `bar_chart` if not found
        }
    }
}


/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(val value: Int) {
    DAY(1),
    WEEK(2),
    MONTH(3),
    CLASS(4),
    GENDER(5);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            DAY to MR.strings.day,
            WEEK to MR.strings.weekly,
            MONTH to MR.strings.monthly,
            CLASS to MR.strings.class_name,
            GENDER to MR.strings.gender_literal
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForXAxis(xAxis: ReportXAxis): StringResource {
            return stringResourceMap[xAxis] ?: MR.strings.day // Fallback to `day` if not found
        }
    }
}


/** Enum representing different time range options for report series */
enum class ReportTimeRange(val value: Int) {
    LAST_WEEK(1),
    LAST_MONTH(2);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            LAST_WEEK to MR.strings.last_week,
            LAST_MONTH to MR.strings.last_month
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForTimeRange(timeRange: ReportTimeRange): StringResource {
            return stringResourceMap[timeRange]
                ?: MR.strings.last_week // Fallback to `last_week` if not found
        }
    }
}

/** Enum representing different filter types for report series */
enum class FilterType(val value: Int) {
    PERSON_AGE(1),
    PERSON_GENDER(2);

    companion object {
        // Map enum values to their corresponding string resources
        private val stringResourceMap = mapOf(
            PERSON_AGE to MR.strings.person_age,
            PERSON_GENDER to MR.strings.person_gender
        )

        // Function to get the string resource for a given enum value
        fun getStringResourceForFilterType(filterType: FilterType): StringResource {
            return stringResourceMap[filterType]
                ?: MR.strings.person_age // Fallback to `PERSON_AGE` if not found
        }
    }
}
