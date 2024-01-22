package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.MR
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzAssignment

object MarkingTypeConstants {

    val MARKING_TYPE_MESSAGE_IDS = listOf(
        MessageIdOption2(MR.strings.teacher, ClazzAssignment.MARKED_BY_COURSE_LEADER),
        MessageIdOption2(MR.strings.students, ClazzAssignment.MARKED_BY_PEERS)
    )

}