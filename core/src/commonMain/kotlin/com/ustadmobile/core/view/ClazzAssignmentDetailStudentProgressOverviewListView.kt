package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.AssignmentProgressSummary


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<PersonWithAttemptsSummary, PersonWithAttemptsSummary> {

    var progressSummary: AssignmentProgressSummary?

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressOverviewListView"
    }

}