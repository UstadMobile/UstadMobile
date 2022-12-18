package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.db.dao.ClazzDaoCommon
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2

object SortConstants {

    val SORT_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_ASC),
        MessageIdOption2(MessageID.name, ClazzDaoCommon.SORT_CLAZZNAME_DESC),
        MessageIdOption2(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_ASC),
        MessageIdOption2(MessageID.attendance, ClazzDaoCommon.SORT_ATTENDANCE_DESC)
    )
}