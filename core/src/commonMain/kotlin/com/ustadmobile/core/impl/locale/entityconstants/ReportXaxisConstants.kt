package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.domain.report.model.ReportTimeRange
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.util.MessageIdOption2

object ReportXAxisConstants {
    val X_AXIS_OPTIONS = listOf(
        MessageIdOption2(MR.strings.day, ReportXAxis.DAY.id),
        MessageIdOption2(MR.strings.weekly, ReportXAxis.WEEK.id),
        MessageIdOption2(MR.strings.classes, ReportXAxis.CLASS.id),
        MessageIdOption2(MR.strings.gender_literal, ReportXAxis.GENDER.id),
        MessageIdOption2(MR.strings.monthly, ReportXAxis.MONTH.id),
    )
}

object ReportSeriesYAxisConstants {
    val Y_AXIS_OPTIONS = listOf(
        MessageIdOption2(MR.strings.number_active_users, ReportSeriesYAxis.NUMBER_ACTIVE_USERS.id),
        MessageIdOption2(MR.strings.average_usage_time_per_user, ReportSeriesYAxis.AVERAGE_USAGE_TIME_PER_USER.id),
        MessageIdOption2(MR.strings.total_absences, ReportSeriesYAxis.TOTAL_ABSENCES.id),
        MessageIdOption2(MR.strings.total_lates, ReportSeriesYAxis.TOTAL_LATES.id),
        MessageIdOption2(MR.strings.number_unique_students_attending, ReportSeriesYAxis.NUMBER_UNIQUE_STUDENTS_ATTENDING.id),
  )
}

object ReportSeriesVisualTypeConstants {
    val VISUAL_TYPE_OPTIONS = listOf(
        MessageIdOption2(MR.strings.bar_chart, ReportSeriesVisualType.BAR_CHART.id),
    )
}

object ReportTimeRangeConstants {
    val TIME_RANGE_OPTIONS = listOf(
        MessageIdOption2(MR.strings.last_week, ReportTimeRange.LAST_WEEK.id),
        MessageIdOption2(MR.strings.last_month, ReportTimeRange.LAST_MONTH.id),
    )
}
object FilterFieldConstants {
    val FILTER_OPTIONS = listOf(
        MessageIdOption2(MR.strings.gender_literal, 1),
        MessageIdOption2(MR.strings.age, 2),
    )
}

