package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submittedCourseAssignmentSubmission: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DataSourceFactory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    var showSubmission: Boolean

    var addTextSubmissionVisible: Boolean

    var addFileSubmissionVisible: Boolean

    var submissionMark: CourseAssignmentMark?

    var submissionStatus: Int

    var unassignedError: String?

    companion object {

        const val VIEW_NAME = "CourseAssignmentDetailOverviewView"

    }

}