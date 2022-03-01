package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmission


interface TextAssignmentEditView: UstadEditView<CourseAssignmentSubmission> {

    companion object {

        const val VIEW_NAME = "TextAssignmentEditView"

    }

}