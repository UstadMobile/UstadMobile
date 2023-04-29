package com.ustadmobile.core.viewmodel.courseblock

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry

class CourseBlockViewModelConstants {

    enum class CompletionCriteria(val messageId: Int, val value: Int) {
        AUTOMATIC(MessageID.automatic, ContentEntry.COMPLETION_CRITERIA_AUTOMATIC),
        MIN_SCORE(MessageID.minimum_score, ContentEntry.COMPLETION_CRITERIA_MIN_SCORE),
        STUDENT_SELF_MARKS(MessageID.student_marks_content, ContentEntry.COMPLETION_CRITERIA_MARKED_BY_STUDENT),
        ASSIGNMENT_SUBMITTED(MessageID.submitted, ClazzAssignment.COMPLETION_CRITERIA_SUBMIT),
        ASSIGNMENT_GRADED(MessageID.graded, ClazzAssignment.COMPLETION_CRITERIA_GRADED);

        companion object {

            fun valueOf(value: Int) : CompletionCriteria {
                return values().firstOrNull { it.value == value } ?: values().first()
            }

        }

    }

}