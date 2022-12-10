package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries

object YAxisConstants {

    val Y_AXIS_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.content_total_duration, ReportSeries.TOTAL_DURATION),
        MessageIdOption2(MessageID.content_average_duration, ReportSeries.AVERAGE_DURATION),
        MessageIdOption2(MessageID.count_session, ReportSeries.NUMBER_SESSIONS),
        MessageIdOption2(MessageID.interaction_recorded, ReportSeries.INTERACTIONS_RECORDED),
        MessageIdOption2(MessageID.number_active_users, ReportSeries.NUMBER_ACTIVE_USERS),
        MessageIdOption2(MessageID.average_usage_time_per_user, ReportSeries.AVERAGE_USAGE_TIME_PER_USER),
        MessageIdOption2(MessageID.number_students_completed, ReportSeries.NUMBER_OF_STUDENTS_COMPLETED_CONTENT),
        MessageIdOption2(MessageID.percent_students_completed, ReportSeries.PERCENT_OF_STUDENTS_COMPLETED_CONTENT),
        MessageIdOption2(MessageID.total_attendances, ReportSeries.TOTAL_ATTENDANCE),
        MessageIdOption2(MessageID.total_absences, ReportSeries.TOTAL_ABSENCES),
        MessageIdOption2(MessageID.total_lates, ReportSeries.TOTAL_LATES),
        MessageIdOption2(MessageID.percent_students_attended, ReportSeries.PERCENTAGE_STUDENTS_ATTENDED),
        MessageIdOption2(MessageID.percent_students_attended_or_late, ReportSeries.PERCENTAGE_STUDENTS_ATTENDED_OR_LATE),
        MessageIdOption2(MessageID.total_number_of_classes, ReportSeries.TOTAL_CLASSES),
        MessageIdOption2(MessageID.number_unique_students_attending, ReportSeries.NUMBER_UNIQUE_STUDENTS_ATTENDING)
    )
}