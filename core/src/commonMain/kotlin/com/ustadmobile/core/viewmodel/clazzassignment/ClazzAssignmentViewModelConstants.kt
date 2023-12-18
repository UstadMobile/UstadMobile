package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission
import dev.icerock.moko.resources.StringResource

object ClazzAssignmentViewModelConstants {

    val SUBMISSION_STAUTUS_MESSAGE_ID = mapOf(
        CourseAssignmentSubmission.SUBMITTED to MR.strings.submitted_key,
        CourseAssignmentSubmission.MARKED to MR.strings.marked_key,
        CourseAssignmentSubmission.NOT_SUBMITTED to MR.strings.not_submitted,
    )

    enum class TextLimitType(val stringResource: StringResource, val value: Int) {
        LIMIT_WORDS(MR.strings.words, ClazzAssignment.TEXT_WORD_LIMIT),
        LIMIT_CHARS(MR.strings.characters, ClazzAssignment.TEXT_CHAR_LIMIT),
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