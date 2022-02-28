package com.ustadmobile.core.view

import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.lib.db.entities.*


interface ClazzAssignmentDetailStudentProgressView: UstadDetailView<ClazzAssignment> {

    var submitMarkError: String?

    var person: Person?

    var clazzCourseAssignmentSubmissionAttachment: DoorDataSourceFactory<Int, CourseAssignmentSubmissionWithAttachment>?

    var clazzAssignmentPrivateComments: DoorDataSourceFactory<Int, CommentsWithPerson>?

    var submissionScore: CourseAssignmentMark?

    var submissionStatus: Int

    var markNextStudentEnabled: Boolean

    companion object {
        const val VIEW_NAME = "ClazzAssignmentDetailStudentProgressListView"
    }

}