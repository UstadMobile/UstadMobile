package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2

object FilterConstants {

    val FILTER_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.currently_enrolled, ClazzDaoCommon.FILTER_CURRENTLY_ENROLLED),
        MessageIdOption2(MessageID.past_enrollments, ClazzDaoCommon.FILTER_PAST_ENROLLMENTS),
        MessageIdOption2(MessageID.all, 0)
    )
}