package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignment


interface ClazzAssignmentDetailView: UstadDetailView<ClazzAssignment> {

    var tabs: List<String>?

    companion object {

        const val VIEW_NAME = "CourseAssignmentDetailView"

    }

}