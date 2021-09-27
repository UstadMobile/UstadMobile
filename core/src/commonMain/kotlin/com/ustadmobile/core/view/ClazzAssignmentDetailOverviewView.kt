package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignment> {

    var clazzMetrics: ContentEntryStatementScoreProgress?
    var clazzAssignmentContent
            : DoorDataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DoorDataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    companion object {

        const val VIEW_NAME = "ClazzAssignmentDetailOverviewView"

    }

}