package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.StatementEntity

object ContentCompletionStatusConstants {

    val CONTENT_COMPLETION_STATUS_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.completed, StatementEntity.CONTENT_COMPLETE),
        MessageIdOption2(MessageID.passed, StatementEntity.CONTENT_PASSED),
        MessageIdOption2(MessageID.failed, StatementEntity.CONTENT_FAILED)
    )
}