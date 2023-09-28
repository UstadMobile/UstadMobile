package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ContentEntry

object CompletionCriteriaConstants {

    val COMPLETION_CRITERIA_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.automatic, ContentEntry.COMPLETION_CRITERIA_AUTOMATIC),
        MessageIdOption2(MR.strings.minimum_score, ContentEntry.COMPLETION_CRITERIA_MIN_SCORE),
        MessageIdOption2(MR.strings.student_marks_content, ContentEntry.COMPLETION_CRITERIA_MARKED_BY_STUDENT),

    )
}