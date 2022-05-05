package com.ustadmobile.core.controller

import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionWithAttachment

interface FileSubmissionListItemListener {

    fun onClickDeleteSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment)

    fun onClickOpenSubmission(submissionCourse: CourseAssignmentSubmissionWithAttachment)

}