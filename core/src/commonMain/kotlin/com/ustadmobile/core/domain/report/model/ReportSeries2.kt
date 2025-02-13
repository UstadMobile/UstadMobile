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
enum class ReportSeriesYAxis(override val label: StringResource) : OptionWithLabelStringResource {
    TOTAL_DURATION(MR.strings.total_duration),
    AVERAGE_DURATION(MR.strings.average_duration),
    NUMBER_SESSIONS(MR.strings.number_sessions),
    INTERACTIONS_RECORDED(MR.strings.interactions_recorded),
    NUMBER_ACTIVE_USERS(MR.strings.number_active_users),
    AVERAGE_USAGE_TIME_PER_USER(MR.strings.average_usage_time_per_user),
    NUMBER_OF_STUDENTS_COMPLETED_CONTENT(MR.strings.number_of_students_completed_content),
    PERCENT_OF_STUDENTS_COMPLETED_CONTENT(MR.strings.percent_of_students_completed_content),
    TOTAL_ATTENDANCE(MR.strings.total_attendance),
    TOTAL_ABSENCES(MR.strings.total_absences),
    TOTAL_LATES(MR.strings.total_lates),
    PERCENTAGE_STUDENTS_ATTENDED(MR.strings.percentage_students_attended),
    PERCENTAGE_STUDENTS_ATTENDED_OR_LATE(MR.strings.percentage_students_attended_or_late),
    TOTAL_CLASSES(MR.strings.total_classes),
    NUMBER_UNIQUE_STUDENTS_ATTENDING(MR.strings.number_unique_students_attending),
    NONE(MR.strings.none),
    ATTENDANCE_QUERY(MR.strings.attendance_query),
    STATEMENT_QUERY(MR.strings.statement_query);
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(override val label: StringResource) :
    OptionWithLabelStringResource {
    BAR_CHART(MR.strings.bar_chart),
    LINE_GRAPH(MR.strings.line_chart);
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(override val label: StringResource) : OptionWithLabelStringResource {
    DAY(MR.strings.day),
    WEEK(MR.strings.weekly),
    MONTH(MR.strings.monthly),
    CLASS(MR.strings.class_name),
    GENDER(MR.strings.gender_literal),
    NONE(MR.strings.none);
}

/** Enum representing different filter types for report series */
enum class FilterType(override val label: StringResource) : OptionWithLabelStringResource {
    PERSON_AGE(MR.strings.person_age),
    PERSON_GENDER(MR.strings.person_gender);
}

/** Enum representing different time range options for report series */
enum class ReportTimeRange(override val label: StringResource) : OptionWithLabelStringResource {
    LAST_WEEK(MR.strings.last_week),
    LAST_MONTH(MR.strings.last_month);
}

enum class GenderType(override val label: StringResource) : OptionWithLabelStringResource {
    MALE(MR.strings.male),
    FEMALE(MR.strings.female),
    OTHER(MR.strings.other);
}