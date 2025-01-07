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

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.NONE,

    var reportSeriesFilters: List<ReportFilter3>? = null,

    var reportTimeRange: ReportTimeRange? = ReportTimeRange.LAST_WEEK

)

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis(val value: Int, val stringResource: StringResource) {
    TOTAL_DURATION(1, MR.strings.total_duration),
    AVERAGE_DURATION(2, MR.strings.average_duration),
    NUMBER_SESSIONS(3, MR.strings.number_sessions),
    INTERACTIONS_RECORDED(4, MR.strings.interactions_recorded),
    NUMBER_ACTIVE_USERS(5, MR.strings.number_active_users),
    AVERAGE_USAGE_TIME_PER_USER(6, MR.strings.average_usage_time_per_user),
    NUMBER_OF_STUDENTS_COMPLETED_CONTENT(7, MR.strings.number_of_students_completed_content),
    PERCENT_OF_STUDENTS_COMPLETED_CONTENT(8, MR.strings.percent_of_students_completed_content),
    TOTAL_ATTENDANCE(9, MR.strings.total_attendance),
    TOTAL_ABSENCES(10, MR.strings.total_absences),
    TOTAL_LATES(11, MR.strings.total_lates),
    PERCENTAGE_STUDENTS_ATTENDED(12, MR.strings.percentage_students_attended),
    PERCENTAGE_STUDENTS_ATTENDED_OR_LATE(13, MR.strings.percentage_students_attended_or_late),
    TOTAL_CLASSES(14, MR.strings.total_classes),
    NUMBER_UNIQUE_STUDENTS_ATTENDING(15, MR.strings.number_unique_students_attending),
    NONE(16, MR.strings.none),
    ATTENDANCE_QUERY(17, MR.strings.attendance_query),
    STATEMENT_QUERY(18, MR.strings.statement_query);
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(val value: Int, val stringResource: StringResource) {
    BAR_CHART(1, MR.strings.bar_chart),
    LINE_GRAPH(2, MR.strings.line_chart);
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(val value: Int, val stringResource: StringResource) {
    DAY(1, MR.strings.day),
    WEEK(2, MR.strings.weekly),
    MONTH(3, MR.strings.monthly),
    CLASS(4, MR.strings.class_name),
    GENDER(5, MR.strings.gender_literal),
    NONE(6, MR.strings.none);

}

/** Enum representing different filter types for report series */
enum class FilterType(val value: Int, val stringResource: StringResource) {
    PERSON_AGE(1, MR.strings.person_age),
    PERSON_GENDER(2, MR.strings.person_gender);
}


/** Enum representing different time range options for report series */
enum class ReportTimeRange(val value: Int, val stringResource: StringResource) {
    LAST_WEEK(1, MR.strings.last_week),
    LAST_MONTH(2, MR.strings.last_month);
}

enum class GenderType(val value: Int, val stringResource: StringResource){
    MALE(1, MR.strings.male),
    FEMALE(2, MR.strings.female),
    OTHER(3, MR.strings.other);
}
