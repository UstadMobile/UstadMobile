package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics


interface ClazzAssignmentListView: UstadListView<ClazzAssignmentWithMetrics, ClazzAssignmentWithMetrics> {

    var clazzTimeZone: String?

    companion object {
        const val VIEW_NAME = "ClassAssignmentListView"
    }

}