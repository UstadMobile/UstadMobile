package com.ustadmobile.core.view

import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailOverviewView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submittedCourseAssignmentSubmission: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var markList: DataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>?

    var addedCourseAssignmentSubmission: List<CourseAssignmentSubmissionWithAttachment>?

    var gradeFilterChips: List<ListFilterIdOption>?

    var timeZone: String?

    var clazzAssignmentClazzComments: DataSourceFactory<Int, CommentsWithPerson>?
    var clazzAssignmentPrivateComments: DataSourceFactory<Int, CommentsWithPerson>?

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