package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission

fun CourseAssignmentSubmission.textLength(limitType: Int) : Int? {
    return if(limitType == ClazzAssignment.TEXT_WORD_LIMIT) {
        casText?.htmlToPlainText()?.countWords() ?: 0
    }else if(limitType == ClazzAssignment.TEXT_CHAR_LIMIT) {
        casText?.htmlToPlainText()?.length ?: 0
    }else {
        null
    }
}