package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

object ClazzAssignmentViewModelConstants {

    val SUBMISSION_STAUTUS_MESSAGE_ID = mapOf(
        CourseAssignmentSubmission.SUBMITTED to MessageID.submitted,
        CourseAssignmentSubmission.MARKED to MessageID.marked,
        CourseAssignmentSubmission.NOT_SUBMITTED to MessageID.not_submitted,
    )

    enum class TextLimitType(val messageId: Int, val value: Int) {
        LIMIT_WORDS(MessageID.words, ClazzAssignment.TEXT_WORD_LIMIT),
        LIMIT_CHARS(MessageID.characters, ClazzAssignment.TEXT_CHAR_LIMIT),
    }

    enum class MarkingType(val value: Int) {
        TEACHER(ClazzAssignment.MARKED_BY_COURSE_LEADER),
        PEERS(ClazzAssignment.MARKED_BY_PEERS);

        companion object {

            fun valueOf(value: Int): MarkingType {
                return values().firstOrNull { it.value == value } ?: values().first()
            }
        }
    }

}