package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment

object ClazzAssignmentViewModelConstants {

    enum class TextLimitType(val messageId: Int, val value: Int) {
        LIMIT_WORDS(MessageID.words, ClazzAssignment.TEXT_WORD_LIMIT),
        LIMIT_CHARS(MessageID.characters, ClazzAssignment.TEXT_CHAR_LIMIT),
    }

}