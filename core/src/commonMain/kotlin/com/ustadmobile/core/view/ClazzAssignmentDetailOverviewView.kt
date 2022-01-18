package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignment> {


    var clazzMetrics: ContentEntryStatementScoreProgress?
    var clazzAssignmentContent
            : DoorDataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?

    var clazzAssignmentFileSubmission: DoorDataSourceFactory<Int, AssignmentFileSubmission>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DoorDataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    var showFileSubmission: Boolean

    var hasPassedDeadline: Boolean

    var maxNumberOfFilesSubmission: Int

    companion object {

        const val VIEW_NAME = "ClazzAssignmentDetailOverviewView"

    }

}