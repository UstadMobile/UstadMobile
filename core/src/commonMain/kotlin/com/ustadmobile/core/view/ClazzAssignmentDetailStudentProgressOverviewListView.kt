package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


interface ClazzAssignmentDetailStudentProgressOverviewListView: UstadListView<ClazzAssignmentWithMetrics, ClazzAssignmentWithMetrics> {

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressOverviewListView"
    }

}