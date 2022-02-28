package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment

interface FileSubmissionListItemListener {

    fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment)

    fun onClickOpenFileSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment)

}