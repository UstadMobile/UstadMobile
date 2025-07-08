package com.ustadmobile.core.domain.report.model

import com.ustadmobile.core.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.datetime.DatePeriod
import kotlinx.serialization.Serializable

@Serializable
data class ReportSeries2(

    val reportSeriesUid: Int = 0,

    val reportSeriesTitle: String = "Series 1",

    val reportSeriesYAxis: ReportSeriesYAxis = ReportSeriesYAxis.TOTAL_DURATION,

    val reportSeriesVisualType: ReportSeriesVisualType = ReportSeriesVisualType.BAR_CHART,

    val reportSeriesSubGroup: ReportXAxis? = ReportXAxis.DAY,

    val reportSeriesFilters: List<ReportFilter3>? = null,

)

enum class YAxisTypes(
    override val label: StringResource,
) : OptionWithLabelStringResource {
    COUNT(MR.strings.count), DURATION(MR.strings.duration),
}

/** Enum representing different Y-axis or report series options */
enum class ReportSeriesYAxis(
    override val label: StringResource,
    val type: YAxisTypes
) : OptionWithLabelStringResource {
    TOTAL_DURATION(MR.strings.total_duration, YAxisTypes.DURATION),
    AVERAGE_DURATION(MR.strings.average_duration, YAxisTypes.DURATION),
    NUMBER_SESSIONS(MR.strings.number_sessions, YAxisTypes.COUNT),
    INTERACTIONS_RECORDED(MR.strings.interactions_recorded, YAxisTypes.COUNT),
    NUMBER_ACTIVE_USERS(MR.strings.number_active_users, YAxisTypes.COUNT),
    AVERAGE_USAGE_TIME_PER_USER(MR.strings.average_usage_time_per_user, YAxisTypes.DURATION),
}

/** Enum representing different visual types for report series */
enum class ReportSeriesVisualType(override val label: StringResource) :
    OptionWithLabelStringResource {
    BAR_CHART(MR.strings.bar_chart),
    LINE_GRAPH(MR.strings.line_chart);
}

/** Enum representing different X-axis or sub-group options for report series */
enum class ReportXAxis(
    override val label: StringResource,
    val personJoinRequired: Boolean = false,
    val datePeriod: DatePeriod? = null,
) : OptionWithLabelStringResource {
    /**
     * Displayed to the user using the localized date formatted for the specified date
     */
    DAY(MR.strings.day, datePeriod = DatePeriod(days = 1)),

    /**
     * When report data xAxis is by week, or data is subgrouped by week, this is based on the day of
     * the week of the first day of the reporting period. E.g. if the report period is Tuesday
     * 4/Feb/25 to Monday 17/Feb/25, then there will be two entries on the xAxis: 2025-02-04, and
     * 2025-02-11.
     *
     * Displayed to the user using the localized date formatted for the specified date
     */
    WEEK(MR.strings.weekly, datePeriod = DatePeriod(days = 7)),

    /**
     * When report data xAxis is by month, or data is subgrouped by month, this will be done by
     * calendar month. Queries will group data using YYYY-MM-01 e.g. using DATE_TRUNC('month'..)
     * on PostgreSQL and the strftime 'start of month' modifier on SQLite.
     *
     * Displayed to the user as Month - Year using localized date formatter
     */
    MONTH(MR.strings.monthly, datePeriod = DatePeriod(months = 1)),

    /**
     * When report data xAxis is by month, or data is subgrouped by month, this will be done by
     * calendar year. Queries will group data using YYYY-01-01 e.g. using DATE_TRUNC('year'..)
     * on PostgreSQL and the strftime 'start of year' modifier on SQLite.
     *
     * Displayed to the user as the year (only)
     */
    YEAR(MR.strings.year, datePeriod = DatePeriod(years = 1)),

    /**
     * Displayed to the user as the clazz name. RunReportUseCaseDbImpl will substitute the clazzUid
     * included in the query with the clazz name
     */
    CLASS(MR.strings.class_name),

    /**
     * Displayed to the user using the localized string as per their locale (e.g. male, female..)
     */
    GENDER(MR.strings.gender_literal, personJoinRequired = true),
}

/** Enum representing different filter types for report series */
enum class FilterType(override val label: StringResource) : OptionWithLabelStringResource {
    PERSON_AGE(MR.strings.person_age),
    PERSON_GENDER(MR.strings.person_gender);
}


enum class GenderType(override val label: StringResource) : OptionWithLabelStringResource {
    MALE(MR.strings.male),
    FEMALE(MR.strings.female),
    OTHER(MR.strings.other);
}


