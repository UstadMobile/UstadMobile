package com.ustadmobile.core.view

import com.ustadmobile.core.util.ListFilterIdOption
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submitMarkError: String?

    var submitterName: String?

    var gradeFilterChips: List<ListFilterIdOption>?

    var clazzCourseAssignmentSubmissionAttachment: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var markList: DataSourceFactory<Int, CourseAssignmentMarkWithPersonMarker>?

    var clazzAssignmentPrivateComments: DataSourceFactory<Int, CommentsWithPerson>?

    var submissionScore: AverageCourseAssignmentMark?

    var submissionStatus: Int

    var markNextStudentVisible: Boolean

    var submitButtonVisible: Boolean

    companion object {
        const val VIEW_NAME = "CourseAssignmentDetailStudentProgressListView"
    }

}