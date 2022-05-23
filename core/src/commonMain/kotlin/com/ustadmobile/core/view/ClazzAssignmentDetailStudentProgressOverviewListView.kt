package com.ustadmobile.core.view

import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary
import com.ustadmobile.lib.db.entities.AssignmentSubmitterSummary


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<AssignmentSubmitterSummary, AssignmentSubmitterSummary> {

    var progressSummary: DoorLiveData<AssignmentProgressSummary?>?

    companion object {
        const val VIEW_NAME = "CourseAssignmentDetailStudentProgressOverviewListView"
    }

}