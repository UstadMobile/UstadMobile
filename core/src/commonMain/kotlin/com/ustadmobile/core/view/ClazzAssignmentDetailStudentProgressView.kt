package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignment


interface ClazzAssignmentDetailStudentProgressView: UstadListView<ClazzAssignment, ClazzAssignment> {

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressListView"
    }

}