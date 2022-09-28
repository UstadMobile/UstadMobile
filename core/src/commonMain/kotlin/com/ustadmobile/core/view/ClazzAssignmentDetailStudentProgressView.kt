package com.ustadmobile.core.view

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.lib.db.entities.ClazzAssignmentWithCourseBlock
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.CourseAssignmentMark
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignmentWithCourseBlock> {

    var submitMarkError: String?

    var submitterName: String?

    var clazzCourseAssignmentSubmissionAttachment: DataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var clazzAssignmentPrivateComments: DataSourceFactory<Int, CommentsWithPerson>?

    var submissionScore: CourseAssignmentMark?

    var submissionStatus: Int

    var markNextStudentVisible: Boolean

    var submitButtonVisible: Boolean

    companion object {
        const val VIEW_NAME = "CourseAssignmentDetailStudentProgressListView"
    }

}