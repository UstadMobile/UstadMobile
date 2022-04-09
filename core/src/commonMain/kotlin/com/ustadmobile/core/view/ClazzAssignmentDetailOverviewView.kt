package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submittedCourseAssignmentSubmission: DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DoorDataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    var showSubmission: Boolean

    var hasPassedDeadline: Boolean

    var maxNumberOfFilesSubmission: Int

    var submissionMark: CourseAssignmentMark?

    var submissionStatus: Int

    companion object {

        const val VIEW_NAME = "CourseAssignmentDetailOverviewView"

    }

}