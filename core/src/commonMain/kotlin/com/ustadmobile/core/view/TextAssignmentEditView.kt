package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission


interface TextAssignmentEditView: UstadEditView<CourseAssignmentSubmission> {

    var clazzAssignment: ClazzAssignment?

    companion object {

        const val VIEW_NAME = "TextAssignmentEditView"

        const val ASSIGNMENT_ID = "assignment"

        const val EDIT_ENABLED = "editEnabled"

    }

}