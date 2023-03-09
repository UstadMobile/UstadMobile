package com.ustadmobile.core.view

import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<AssignmentSubmitterSummary, AssignmentSubmitterSummary> {

    var progressSummary: LiveData<AssignmentProgressSummary?>?

    companion object {
        const val VIEW_NAME = "CourseAssignmentDetailStudentProgressOverviewListView"
    }

}