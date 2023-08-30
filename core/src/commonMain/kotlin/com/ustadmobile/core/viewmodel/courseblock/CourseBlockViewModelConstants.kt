package com.ustadmobile.core.viewmodel.courseblock

import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ContentEntry
import dev.icerock.moko.resources.StringResource

class CourseBlockViewModelConstants {

    @kotlinx.serialization.Serializable
    enum class CompletionCriteria(val stringResource: StringResource, val value: Int) {
        AUTOMATIC(MR.strings.automatic, ContentEntry.COMPLETION_CRITERIA_AUTOMATIC),
        MIN_SCORE(MR.strings.minimum_score, ContentEntry.COMPLETION_CRITERIA_MIN_SCORE),
        STUDENT_SELF_MARKS(MR.strings.student_marks_content, ContentEntry.COMPLETION_CRITERIA_MARKED_BY_STUDENT),
        ASSIGNMENT_SUBMITTED(MR.strings.submitted_key, ClazzAssignment.COMPLETION_CRITERIA_SUBMIT),
        ASSIGNMENT_GRADED(MR.strings.graded, ClazzAssignment.COMPLETION_CRITERIA_GRADED);

        companion object {

            fun valueOf(value: Int) : CompletionCriteria {
                return values().firstOrNull { it.value == value } ?: values().first()
            }

        }

    }

}