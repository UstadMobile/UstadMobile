package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.PersonWithAttemptsSummary


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<PersonWithAttemptsSummary, PersonWithAttemptsSummary> {

    var clazzAssignmentWithMetrics: ClazzAssignmentWithMetrics?

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressOverviewListView"
    }

}