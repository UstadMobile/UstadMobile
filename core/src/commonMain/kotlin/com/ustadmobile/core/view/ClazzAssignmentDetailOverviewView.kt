package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submittedCourseAssignmentSubmission: DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var markList: DoorDataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>?

    var addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DoorDataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var showPrivateComments: Boolean

    var showSubmission: Boolean

    var addTextSubmissionVisible: Boolean

    var addFileSubmissionVisible: Boolean

    var submissionMark: AverageCourseAssignmentMark?

    var submissionStatus: Int

    var unassignedError: String?

    companion object {

        const val VIEW_NAME = "CourseAssignmentDetailOverviewView"

    }

}