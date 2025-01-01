package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MessageIdOption2

object ReportXAxisConstants {
    val X_AXIS_OPTIONS = listOf(
        MessageIdOption2(MR.strings.day, ReportXAxis.DAY.value),
        MessageIdOption2(MR.strings.weekly, ReportXAxis.WEEK.value),
        MessageIdOption2(MR.strings.classes, ReportXAxis.CLASS.value),
        MessageIdOption2(MR.strings.gender_literal, ReportXAxis.GENDER.value),
        MessageIdOption2(MR.strings.monthly, ReportXAxis.MONTH.value),
    )
}

object ReportSeriesYAxisConstants {
    val Y_AXIS_OPTIONS = listOf(
        MessageIdOption2(MR.strings.total_duration, ReportSeriesYAxis.TOTAL_DURATION.value),
        MessageIdOption2(MR.strings.average_duration, ReportSeriesYAxis.AVERAGE_DURATION.value),
        MessageIdOption2(MR.strings.number_sessions, ReportSeriesYAxis.NUMBER_SESSIONS.value),
        MessageIdOption2(MR.strings.interactions_recorded, ReportSeriesYAxis.INTERACTIONS_RECORDED.value),
        MessageIdOption2(MR.strings.number_active_users, ReportSeriesYAxis.NUMBER_ACTIVE_USERS.value),
        MessageIdOption2(MR.strings.average_usage_time_per_user, ReportSeriesYAxis.AVERAGE_USAGE_TIME_PER_USER.value),
        MessageIdOption2(MR.strings.number_of_students_completed_content, ReportSeriesYAxis.NUMBER_OF_STUDENTS_COMPLETED_CONTENT.value),
        MessageIdOption2(MR.strings.percent_of_students_completed_content, ReportSeriesYAxis.PERCENT_OF_STUDENTS_COMPLETED_CONTENT.value),
        MessageIdOption2(MR.strings.total_attendance, ReportSeriesYAxis.TOTAL_ATTENDANCE.value),
        MessageIdOption2(MR.strings.total_absences, ReportSeriesYAxis.TOTAL_ABSENCES.value),
        MessageIdOption2(MR.strings.total_lates, ReportSeriesYAxis.TOTAL_LATES.value),
        MessageIdOption2(MR.strings.percentage_students_attended, ReportSeriesYAxis.PERCENTAGE_STUDENTS_ATTENDED.value),
        MessageIdOption2(MR.strings.percentage_students_attended_or_late, ReportSeriesYAxis.PERCENTAGE_STUDENTS_ATTENDED_OR_LATE.value),
        MessageIdOption2(MR.strings.total_classes, ReportSeriesYAxis.TOTAL_CLASSES.value),
        MessageIdOption2(MR.strings.number_unique_students_attending, ReportSeriesYAxis.NUMBER_UNIQUE_STUDENTS_ATTENDING.value),
        MessageIdOption2(MR.strings.none, ReportSeriesYAxis.NONE.value),
        MessageIdOption2(MR.strings.attendance_query, ReportSeriesYAxis.ATTENDANCE_QUERY.value),
        MessageIdOption2(MR.strings.statement_query, ReportSeriesYAxis.STATEMENT_QUERY.value)
    )
}


object ReportSeriesVisualTypeConstants {
    val VISUAL_TYPE_OPTIONS = listOf(
        MessageIdOption2(MR.strings.bar_chart, ReportSeriesVisualType.BAR_CHART.value),
        MessageIdOption2(MR.strings.line_chart, ReportSeriesVisualType.LINE_GRAPH.value),

        )
}

object ReportTimeRangeConstants {
    val TIME_RANGE_OPTIONS = listOf(
        MessageIdOption2(MR.strings.last_week, ReportTimeRange.LAST_WEEK.value),
        MessageIdOption2(MR.strings.last_month, ReportTimeRange.LAST_MONTH.value),
    )
}
object FilterFieldConstants {
    val FILTER_OPTIONS = listOf(
        MessageIdOption2(MR.strings.person_gender, FilterType.PERSON_GENDER.value),
        MessageIdOption2(MR.strings.person_age, FilterType.PERSON_AGE.value),
    )
}
object ComparisonConstants {
    val COMPARISON_OPTIONS = listOf(
        MessageIdOption2(MR.strings.equals, Comparisons.EQUALS.value),
        MessageIdOption2(MR.strings.not_equals, Comparisons.NOT_EQUALS.value),
        MessageIdOption2(MR.strings.greater, Comparisons.GREATER.value),
        MessageIdOption2(MR.strings.lesser, Comparisons.LESSER.value),
        MessageIdOption2(MR.strings.between, Comparisons.BETWEEN.value),
    )
}

