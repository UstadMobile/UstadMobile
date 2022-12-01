package com.ustadmobile.core.impl.locale.entityconstants

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.entities.ClazzAssignment

object TextLimitTypeConstants {

    val TEXT_LIMIT_TYPE_MESSAGE_IDS = listOf(
        MessageIdOption2(MessageID.words, ClazzAssignment.TEXT_WORD_LIMIT),
        MessageIdOption2(MessageID.characters, ClazzAssignment.TEXT_CHAR_LIMIT)
    )
}