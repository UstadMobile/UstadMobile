package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignment> {

    var submitMarkError: String?

    var person: Person?
    var studentScore: ContentEntryStatementScoreProgress?

    var clazzAssignmentContent
            : DoorDataSourceFactory<Int, ContentWithAttemptSummary>?

    var clazzAssignmentFileSubmission: DoorDataSourceFactory<Int, AssignmentFileSubmission>?

    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var hasFileSubmission: Boolean

    var markNextStudentEnabled: Boolean

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressListView"
    }

}