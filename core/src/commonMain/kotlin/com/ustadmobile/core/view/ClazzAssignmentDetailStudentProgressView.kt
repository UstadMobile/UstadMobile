package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignment> {

    var person: Person?
    var studentScore: ContentEntryStatementScoreProgress?

    var clazzAssignmentContent
            : DoorDataSourceFactory<Int, ContentWithAttemptSummary>?

    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    companion object {
        const val VIEW_NAME = "ClassAssignmentDetailStudentProgressListView"
    }

}