package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.xapi.StatementEntity

object ContentCompletionStatusConstants {

    val CONTENT_COMPLETION_STATUS_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.completed, StatementEntity.CONTENT_COMPLETE),
        MessageIdOption2(MR.strings.passed, StatementEntity.CONTENT_PASSED),
        MessageIdOption2(MR.strings.failed, StatementEntity.CONTENT_FAILED)
    )
}