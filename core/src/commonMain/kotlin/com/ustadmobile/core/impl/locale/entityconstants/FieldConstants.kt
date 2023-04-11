package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ReportFilter

object FieldConstants {

    val FIELD_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.field_person_gender, ReportFilter.FIELD_PERSON_GENDER),
        MessageIdOption2(MessageID.field_person_age, ReportFilter.FIELD_PERSON_AGE),
        MessageIdOption2(MessageID.field_content_completion, ReportFilter.FIELD_CONTENT_COMPLETION),
        MessageIdOption2(MessageID.field_content_entry, ReportFilter.FIELD_CONTENT_ENTRY),
        MessageIdOption2(MessageID.field_content_progress, ReportFilter.FIELD_CONTENT_PROGRESS),
        MessageIdOption2(MessageID.field_attendance_percentage, ReportFilter.FIELD_ATTENDANCE_PERCENTAGE),
        MessageIdOption2(MessageID.class_enrolment_outcome, ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME),
        MessageIdOption2(MessageID.class_enrolment_leaving, ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON)
    )

}