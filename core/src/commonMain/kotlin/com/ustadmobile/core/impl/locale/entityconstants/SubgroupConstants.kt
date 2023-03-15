package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportSeries

object SubgroupConstants {

    val SUB_GROUP_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.None, ReportSeries.NONE),
        MessageIdOption2(MessageID.day, Report.DAY),
        MessageIdOption2(MessageID.xapi_week, Report.WEEK),
        MessageIdOption2(MessageID.xapi_month, Report.MONTH),
        MessageIdOption2(MessageID.xapi_content_entry, Report.CONTENT_ENTRY),
        MessageIdOption2(MessageID.gender_literal, Report.GENDER),
        MessageIdOption2(MessageID.clazz, Report.CLASS),
        MessageIdOption2(MessageID.class_enrolment_outcome, Report.ENROLMENT_OUTCOME),
        MessageIdOption2(MessageID.class_enrolment_leaving, Report.ENROLMENT_LEAVING_REASON)
    )
}