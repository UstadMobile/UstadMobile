package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignment> {

    var person: Person?
    var studentScore: ContentEntryStatementScoreProgress?

    var clazzAssignmentContent
            : DataSource.Factory<Int, ContentWithAttemptSummary>?

    var clazzAssignmentPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressListView"
    }

}