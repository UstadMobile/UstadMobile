package com.ustadmobile.core.view

import androidx.paging.DataSource
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithMetrics
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignment> {

    var clazzMetrics: ClazzAssignmentWithMetrics?
    var clazzAssignmentContent
            : DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DataSource.Factory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DataSource.Factory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    companion object {

        const val VIEW_NAME = "ClazzAssignmentDetailOverviewView"

    }

}