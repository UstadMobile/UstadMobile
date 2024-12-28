package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.MessageIdOption3

object ReportXAxisConstants {
    val X_AXIS_OPTIONS = listOf(
        MessageIdOption3(MR.strings.day, ReportXAxis.DAY.name),
        MessageIdOption3(MR.strings.weekly, ReportXAxis.WEEK.name),
        MessageIdOption3(MR.strings.classes, ReportXAxis.CLASS.name),
        MessageIdOption3(MR.strings.gender_literal, ReportXAxis.GENDER.name),
        MessageIdOption3(MR.strings.monthly, ReportXAxis.MONTH.name),
    )
}

object ReportSeriesYAxisConstants {
    val Y_AXIS_OPTIONS = listOf(
        MessageIdOption3(MR.strings.total_duration, ReportSeriesYAxis.TOTAL_DURATION.name),
        MessageIdOption3(MR.strings.average_duration, ReportSeriesYAxis.AVERAGE_DURATION.name),
        MessageIdOption3(MR.strings.number_sessions, ReportSeriesYAxis.NUMBER_SESSIONS.name),
        MessageIdOption3(MR.strings.interactions_recorded, ReportSeriesYAxis.INTERACTIONS_RECORDED.name),
        MessageIdOption3(MR.strings.number_active_users, ReportSeriesYAxis.NUMBER_ACTIVE_USERS.name),
        MessageIdOption3(MR.strings.average_usage_time_per_user, ReportSeriesYAxis.AVERAGE_USAGE_TIME_PER_USER.name),
        MessageIdOption3(MR.strings.number_of_students_completed_content, ReportSeriesYAxis.NUMBER_OF_STUDENTS_COMPLETED_CONTENT.name),
        MessageIdOption3(MR.strings.percent_of_students_completed_content, ReportSeriesYAxis.PERCENT_OF_STUDENTS_COMPLETED_CONTENT.name),
        MessageIdOption3(MR.strings.total_attendance, ReportSeriesYAxis.TOTAL_ATTENDANCE.name),
        MessageIdOption3(MR.strings.total_absences, ReportSeriesYAxis.TOTAL_ABSENCES.name),
        MessageIdOption3(MR.strings.total_lates, ReportSeriesYAxis.TOTAL_LATES.name),
        MessageIdOption3(MR.strings.percentage_students_attended, ReportSeriesYAxis.PERCENTAGE_STUDENTS_ATTENDED.name),
        MessageIdOption3(MR.strings.percentage_students_attended_or_late, ReportSeriesYAxis.PERCENTAGE_STUDENTS_ATTENDED_OR_LATE.name),
        MessageIdOption3(MR.strings.total_classes, ReportSeriesYAxis.TOTAL_CLASSES.name),
        MessageIdOption3(MR.strings.number_unique_students_attending, ReportSeriesYAxis.NUMBER_UNIQUE_STUDENTS_ATTENDING.name),
        MessageIdOption3(MR.strings.none, ReportSeriesYAxis.NONE.name),
        MessageIdOption3(MR.strings.attendance_query, ReportSeriesYAxis.ATTENDANCE_QUERY.name),
        MessageIdOption3(MR.strings.statement_query, ReportSeriesYAxis.STATEMENT_QUERY.name)
    )
}


object ReportSeriesVisualTypeConstants {
    val VISUAL_TYPE_OPTIONS = listOf(
        MessageIdOption3(MR.strings.bar_chart, ReportSeriesVisualType.BAR_CHART.name),
        MessageIdOption3(MR.strings.line_chart, ReportSeriesVisualType.LINE_GRAPH.name),

        )
}

object ReportTimeRangeConstants {
    val TIME_RANGE_OPTIONS = listOf(
        MessageIdOption3(MR.strings.last_week, ReportTimeRange.LAST_WEEK.name),
        MessageIdOption3(MR.strings.last_month, ReportTimeRange.LAST_MONTH.name),
    )
}
object FilterFieldConstants {
    val FILTER_OPTIONS = listOf(
        MessageIdOption3(MR.strings.person_gender, FilterType.PERSON_GENDER.name),
        MessageIdOption3(MR.strings.person_age, FilterType.PERSON_AGE.name),
    )
}
