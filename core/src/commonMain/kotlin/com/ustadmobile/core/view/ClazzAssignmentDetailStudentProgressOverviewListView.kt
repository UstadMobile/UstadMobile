package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary
import com.ustadmobile.lib.db.entities.StudentAssignmentProgress


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<PersonWithAttemptsSummary, PersonWithAttemptsSummary> {

    var studentProgress: StudentAssignmentProgress?

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressOverviewListView"
    }

}