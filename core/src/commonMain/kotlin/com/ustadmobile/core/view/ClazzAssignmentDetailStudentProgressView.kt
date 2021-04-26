package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntryWithAttemptsSummary


interface ClazzAssignmentDetailStudentProgressView: UstadListView<ContentEntryWithAttemptsSummary, ContentEntryWithAttemptsSummary> {

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressListView"
    }

}